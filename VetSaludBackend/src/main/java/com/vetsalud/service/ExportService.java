/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.vetsalud.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.vetsalud.dto.MascotaDto;
import com.vetsalud.dto.PropietarioDto;
import com.vetsalud.dto.VeterinarioDto;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Genera archivos Excel (Apache POI) y PDF (iText) a partir de los DTO.
 * Cada listado se convierte primero en una {@link Tabla} (títulos + filas) y los dos
 * formatos se dibujan desde esa misma tabla, así no se repite código por entidad.
 */
public class ExportService {

    /** Datos tabulares listos para dibujar. Las celdas numéricas (Number) se exportan como número en Excel. */
    public record Tabla(String titulo, String hoja, String archivoBase, List<String> encabezados, List<List<Object>> filas) {}

    // ---------- tablas por entidad ----------
    public Tabla mascotas() {
        List<List<Object>> filas = new ArrayList<>();
        for (MascotaDto m : new MascotaService().listar(null)) {
            filas.add(fila(m.getId(), m.getNombre(), m.getEspecie(), m.getRaza(), m.getEdad(), m.getPropietarioNombre()));
        }
        return new Tabla("LISTADO DE MASCOTAS", "Mascotas", "mascotas",
                List.of("ID", "Nombre", "Especie", "Raza", "Edad", "Propietario"), filas);
    }

    public Tabla propietarios() {
        List<List<Object>> filas = new ArrayList<>();
        for (PropietarioDto p : new PropietarioService().listar()) {
            filas.add(fila(p.getId(), p.getDocumento(), p.getNombre(), p.getTelefono(), p.getEmail()));
        }
        return new Tabla("LISTADO DE PROPIETARIOS", "Propietarios", "propietarios",
                List.of("ID", "Documento", "Nombre", "Teléfono", "Email"), filas);
    }

    public Tabla veterinarios() {
        List<List<Object>> filas = new ArrayList<>();
        for (VeterinarioDto v : new VeterinarioService().listar()) {
            filas.add(fila(v.getId(), v.getTarjetaProfesional(), v.getNombre(), v.getEspecialidad()));
        }
        return new Tabla("LISTADO DE VETERINARIOS", "Veterinarios", "veterinarios",
                List.of("ID", "Tarjeta profesional", "Nombre", "Especialidad"), filas);
    }

    private static List<Object> fila(Object... valores) {
        return java.util.Arrays.asList(valores); // permite nulls
    }

    // ---------- Excel ----------
    public byte[] excel(Tabla t) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (Workbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet(t.hoja());

            // Estilo del encabezado: negrita y fondo gris
            Font negrita = libro.createFont();
            negrita.setBold(true);
            CellStyle estiloEncabezado = libro.createCellStyle();
            estiloEncabezado.setFont(negrita);
            estiloEncabezado.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            estiloEncabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row encabezado = hoja.createRow(0);
            for (int i = 0; i < t.encabezados().size(); i++) {
                org.apache.poi.ss.usermodel.Cell celda = encabezado.createCell(i);
                celda.setCellValue(t.encabezados().get(i));
                celda.setCellStyle(estiloEncabezado);
                hoja.setColumnWidth(i, 24 * 256); // ancho fijo (autoSizeColumn falla en servidores sin fuentes)
            }

            int numFila = 1;
            for (List<Object> datos : t.filas()) {
                Row fila = hoja.createRow(numFila++);
                for (int i = 0; i < datos.size(); i++) {
                    Object valor = datos.get(i);
                    org.apache.poi.ss.usermodel.Cell celda = fila.createCell(i);
                    if (valor instanceof Number n) {
                        celda.setCellValue(n.doubleValue());
                    } else {
                        // setCellValue(String) guarda texto: un valor que empiece por "=" no se ejecuta como fórmula
                        celda.setCellValue(valor == null ? "" : valor.toString());
                    }
                }
            }
            hoja.createFreezePane(0, 1); // encabezado fijo al desplazarse
            libro.write(salida);
        }
        return salida.toByteArray();
    }

    // ---------- PDF ----------
    public byte[] pdf(Tabla t) throws IOException {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        PdfDocument pdf = new PdfDocument(new PdfWriter(salida));
        try (Document documento = new Document(pdf, PageSize.A4.rotate())) {
            documento.add(new Paragraph(t.titulo()).setBold().setFontSize(16).setTextAlignment(TextAlignment.CENTER));
            documento.add(new Paragraph("Generado el "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    + "  |  Total de registros: " + t.filas().size()).setFontSize(9));

            Table tabla = new Table(t.encabezados().size()).useAllAvailableWidth();
            tabla.setFontSize(9);
            for (String titulo : t.encabezados()) {
                tabla.addHeaderCell(new Cell().add(new Paragraph(titulo).setBold())
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY));
            }
            for (List<Object> datos : t.filas()) {
                for (Object valor : datos) {
                    tabla.addCell(new Cell().add(new Paragraph(valor == null ? "" : valor.toString())));
                }
            }
            documento.add(tabla);
        }
        return salida.toByteArray();
    }
}