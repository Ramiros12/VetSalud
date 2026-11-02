// Panel VetSalud: CRUD de todos los módulos con una configuración por módulo.
const ADMIN = 'ADMINISTRADOR', VET = 'VETERINARIO';

const fmtFecha = v => v ? v.replace('T', ' ').substring(0, 16) : '';
const fmtLista = v => (v || []).join(', ');
const fmtSiNo = v => v ? 'Sí' : 'No';
const fmtFechaHora = v => v ? v.replace('T', ' ').substring(0, 19) : '';

// Entidades que registra la auditoría (los textos deben coincidir con AuditoriaService.java)
const ENTIDADES_AUDITORIA = ['Mascota', 'Propietario', 'Veterinario', 'Historial clínico', 'Usuario', 'Rol'];

const MODULOS = {
    propietarios: {
        titulo: 'Propietarios', url: 'api/propietarios', ver: [ADMIN, VET], escribir: [ADMIN], borrar: [ADMIN],
        columnas: [['documento', 'Documento'], ['nombre', 'Nombre'], ['telefono', 'Teléfono'], ['email', 'Email']],
        campos: [
            { n: 'documento', l: 'Documento', req: true },
            { n: 'nombre', l: 'Nombre completo', req: true },
            { n: 'telefono', l: 'Teléfono' },
            { n: 'email', l: 'Email', t: 'email' }
        ]
    },
    mascotas: {
        titulo: 'Mascotas', url: 'api/mascotas', ver: [ADMIN, VET], escribir: [ADMIN], borrar: [ADMIN],
        columnas: [['nombre', 'Nombre'], ['especie', 'Especie'], ['raza', 'Raza'], ['edad', 'Edad'], ['propietarioNombre', 'Propietario']],
        campos: [
            { n: 'nombre', l: 'Nombre', req: true },
            { n: 'especie', l: 'Especie', req: true },
            { n: 'raza', l: 'Raza' },
            { n: 'edad', l: 'Edad (años)', t: 'number' },
            { n: 'propietarioId', l: 'Propietario', t: 'select', req: true,
              opciones: { url: 'api/propietarios', v: 'id', e: d => d.nombre + ' (' + d.documento + ')' } }
        ]
    },
    veterinarios: {
        titulo: 'Veterinarios', url: 'api/veterinarios', ver: [ADMIN, VET], escribir: [ADMIN], borrar: [ADMIN],
        columnas: [['tarjetaProfesional', 'Tarjeta profesional'], ['nombre', 'Nombre'], ['especialidad', 'Especialidad']],
        campos: [
            { n: 'tarjetaProfesional', l: 'Tarjeta profesional', req: true },
            { n: 'nombre', l: 'Nombre completo', req: true },
            { n: 'especialidad', l: 'Especialidad' }
        ]
    },
    historiales: {
        titulo: 'Historial clínico', url: 'api/historiales', ver: [ADMIN, VET], escribir: [ADMIN, VET], borrar: [ADMIN],
        filtroMascota: true,
        columnas: [['fechaConsulta', 'Fecha', fmtFecha], ['mascotaNombre', 'Mascota'], ['veterinarioNombre', 'Veterinario'], ['diagnostico', 'Diagnóstico']],
        campos: [
            { n: 'mascotaId', l: 'Mascota', t: 'select', req: true,
              opciones: { url: 'api/mascotas', v: 'id', e: d => d.nombre + ' - ' + d.propietarioNombre } },
            { n: 'veterinarioId', l: 'Veterinario', t: 'select', req: true,
              opciones: { url: 'api/veterinarios', v: 'id', e: d => d.nombre } },
            { n: 'fechaConsulta', l: 'Fecha de la consulta', t: 'datetime-local' },
            { n: 'diagnostico', l: 'Diagnóstico', t: 'textarea', req: true }
        ]
    },
    usuarios: {
        titulo: 'Usuarios', url: 'api/usuarios', ver: [ADMIN], escribir: [ADMIN], borrar: [ADMIN],
        columnas: [['username', 'Usuario'], ['roles', 'Roles', fmtLista], ['activo', 'Activo', fmtSiNo]],
        campos: [
            { n: 'username', l: 'Usuario', req: true },
            { n: 'password', l: 'Contraseña (mínimo 6; al editar, vacío = no cambiar)', t: 'password', reqAlCrear: true },
            { n: 'activo', l: 'Activo', t: 'checkbox', porDefecto: true },
            { n: 'roles', l: 'Roles', t: 'multi', req: true, opciones: { url: 'api/roles', v: 'nombre', e: d => d.nombre } }
        ]
    },
    roles: {
        titulo: 'Roles', url: 'api/roles', ver: [ADMIN], escribir: [ADMIN], borrar: [ADMIN],
        columnas: [['id', 'ID'], ['nombre', 'Nombre']],
        campos: [{ n: 'nombre', l: 'Nombre del rol', req: true }]
    },
    // Solo lectura: sin "Nuevo", "Editar" ni "Eliminar" (escribir y borrar vacíos). Solo ADMINISTRADOR.
    auditoria: {
        titulo: 'Auditoría', url: 'api/auditoria', ver: [ADMIN], escribir: [], borrar: [],
        filtroEntidad: true,
        columnas: [['fechaHora', 'Fecha y hora', fmtFechaHora], ['username', 'Usuario'], ['accion', 'Acción'],
                   ['entidad', 'Entidad'], ['idRegistro', 'ID registro'], ['descripcion', 'Descripción']],
        campos: []
    }
};

let usuario = null, actual = null, datos = [];
const $ = id => document.getElementById(id);
const tiene = lista => lista.some(r => usuario.roles.includes(r));

function esc(v) {
    return String(v ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}
function aviso(texto, tipo = 'ok') {
    $('mensaje').innerHTML = texto ? '<div class="msg ' + tipo + '">' + esc(texto) + '</div>' : '';
}

async function iniciar() {
    usuario = await api('api/session');
    $('quien').textContent = usuario.username + ' (' + usuario.roles.join(', ') + ')';
    $('btnSalir').onclick = logout;
    iniciarHerramientas();

    const visibles = Object.keys(MODULOS).filter(k => tiene(MODULOS[k].ver));
    $('menu').innerHTML = visibles.map(k => '<button data-m="' + k + '">' + esc(MODULOS[k].titulo) + '</button>').join('');
    $('menu').onclick = e => { if (e.target.dataset.m) abrir(e.target.dataset.m); };
    abrir(visibles[0]);
}

async function abrir(clave, filtro = '') {
    actual = clave;
    aviso('');
    document.querySelectorAll('#menu button').forEach(b => b.classList.toggle('activo', b.dataset.m === clave));
    const m = MODULOS[clave];
    try {
        datos = await api(m.url + filtro);
    } catch (err) { aviso(err.message, 'err'); return; }

    let html = '<div class="barra"><h2>' + esc(m.titulo) + '</h2><div>';
    if (m.filtroMascota) {
        const mascotas = await api('api/mascotas');
        html += '<select id="filtro" style="width:auto"><option value="">Todas las mascotas</option>' +
            mascotas.map(x => '<option value="' + x.id + '"' + (filtro.endsWith('=' + x.id) ? ' selected' : '') + '>' +
                esc(x.nombre + ' - ' + x.propietarioNombre) + '</option>').join('') + '</select> ';
    }
    if (m.filtroEntidad) {
        html += '<select id="filtroEntidad" style="width:auto"><option value="">Todas las entidades</option>' +
            ENTIDADES_AUDITORIA.map(n => '<option value="' + esc(n) + '"' +
                (filtro === '?entidad=' + encodeURIComponent(n) ? ' selected' : '') + '>' + esc(n) + '</option>').join('') + '</select> ';
    }
    if (tiene(m.escribir)) html += '<button class="btn" id="btnNuevo">+ Nuevo</button>';
    html += '</div></div>';

    const conAcciones = tiene(m.escribir) || tiene(m.borrar);
    html += '<table><thead><tr>' + m.columnas.map(c => '<th>' + esc(c[1]) + '</th>').join('') + (conAcciones ? '<th></th>' : '') + '</tr></thead><tbody>';
    if (!datos.length) html += '<tr><td class="vacio" colspan="' + (m.columnas.length + 1) + '">Sin registros</td></tr>';
    datos.forEach(d => {
        html += '<tr>' + m.columnas.map(c => '<td>' + esc(c[2] ? c[2](d[c[0]]) : d[c[0]]) + '</td>').join('');
        if (conAcciones) {
            html += '<td>' + (tiene(m.escribir) ? '<button class="btn mini" data-ed="' + d.id + '">Editar</button> ' : '') +
                (tiene(m.borrar) ? '<button class="btn mini rojo" data-del="' + d.id + '">Eliminar</button>' : '') + '</td>';
        }
        html += '</tr>';
    });
    $('contenido').innerHTML = html + '</tbody></table>';

    if ($('btnNuevo')) $('btnNuevo').onclick = () => formulario(null);
    if ($('filtroEntidad')) $('filtroEntidad').onchange = e => abrir(clave, e.target.value ? '?entidad=' + encodeURIComponent(e.target.value) : '');
    if ($('filtro')) $('filtro').onchange = e => abrir(clave, e.target.value ? '?mascotaId=' + e.target.value : '');
    $('contenido').onclick = e => {
        if (e.target.dataset.ed) formulario(datos.find(d => String(d.id) === e.target.dataset.ed));
        if (e.target.dataset.del) eliminar(e.target.dataset.del);
    };
    cargarEstadisticas(); // los contadores se actualizan tras cada alta, edición o baja
}

async function formulario(registro) {
    const m = MODULOS[actual];
    $('dlgTitulo').textContent = (registro ? 'Editar ' : 'Nuevo ') + m.titulo.toLowerCase();
    $('dlgError').innerHTML = '';
    let html = '';
    for (const c of m.campos) {
        const val = registro ? registro[c.n] : c.porDefecto;
        const req = (c.req || (c.reqAlCrear && !registro)) ? ' required' : '';
        html += '<label>' + esc(c.l);
        if (c.t === 'select' || c.t === 'multi') {
            const lista = await api(c.opciones.url);
            const sel = c.t === 'multi' ? (val || []) : [val];
            html += '<select name="' + c.n + '"' + (c.t === 'multi' ? ' multiple size="4"' : '') + req + '>' +
                (c.t === 'select' ? '<option value="">-- Seleccione --</option>' : '') +
                lista.map(o => '<option value="' + esc(o[c.opciones.v]) + '"' + (sel.includes(o[c.opciones.v]) ? ' selected' : '') + '>' +
                    esc(c.opciones.e(o)) + '</option>').join('') + '</select>';
        } else if (c.t === 'textarea') {
            html += '<textarea name="' + c.n + '" rows="4"' + req + '>' + esc(val) + '</textarea>';
        } else if (c.t === 'checkbox') {
            html += '<br><input type="checkbox" name="' + c.n + '"' + (val ? ' checked' : '') + '>';
        } else {
            const v = c.t === 'datetime-local' && val ? val.substring(0, 16) : (c.t === 'password' ? '' : val);
            html += '<input name="' + c.n + '" type="' + (c.t || 'text') + '" value="' + esc(v) + '"' + req + '>';
        }
        html += '</label>';
    }
    html += '<div class="acciones"><button type="button" class="btn sec" id="btnCancelar">Cancelar</button>' +
            '<button type="submit" class="btn">Guardar</button></div>';
    $('dlgForm').innerHTML = html;
    $('btnCancelar').onclick = () => $('dlg').close();
    $('dlgForm').onsubmit = async e => {
        e.preventDefault();
        const cuerpo = {};
        for (const c of m.campos) {
            const el = $('dlgForm').elements[c.n];
            if (c.t === 'checkbox') cuerpo[c.n] = el.checked;
            else if (c.t === 'multi') cuerpo[c.n] = Array.from(el.selectedOptions).map(o => o.value);
            else if (c.t === 'select' || c.t === 'number') cuerpo[c.n] = el.value === '' ? null : Number(el.value);
            else if (c.t === 'datetime-local') cuerpo[c.n] = el.value || null;
            else cuerpo[c.n] = el.value;
        }
        try {
            await api(m.url + (registro ? '/' + registro.id : ''), { method: registro ? 'PUT' : 'POST', body: cuerpo });
            $('dlg').close();
            await abrir(actual);
            aviso(registro ? 'Registro actualizado' : 'Registro creado');
        } catch (err) {
            $('dlgError').innerHTML = '<div class="msg err">' + esc(err.message) + '</div>';
        }
    };
    $('dlg').showModal();
}

async function eliminar(id) {
    if (!confirm('¿Eliminar este registro?')) return;
    try {
        await api(MODULOS[actual].url + '/' + id, { method: 'DELETE' });
        await abrir(actual);
        aviso('Registro eliminado');
    } catch (err) { aviso(err.message, 'err'); }
}

// ---------- Estadísticas ----------
async function cargarEstadisticas() {
    try {
        const d = await api('api/estadisticas');
        ['mascotas', 'propietarios', 'veterinarios', 'historiales'].forEach(k => {
            const el = $(k + '-count');
            if (el) el.textContent = d[k] ?? 0;
        });
    } catch (err) {
        console.error('Error al cargar las estadísticas:', err);
    }
}

// ---------- Exportar y copia de seguridad ----------
function iniciarHerramientas() {
    const menu = $('menuExportar');
    menu.onclick = e => {
        const destino = e.target.dataset.exp; // ej: "mascotas/excel"
        if (!destino) return;
        menu.open = false;
        descargar('api/export/' + destino);
    };
    // cerrar el menú al hacer clic fuera
    document.addEventListener('click', e => { if (menu.open && !menu.contains(e.target)) menu.open = false; });

    if (usuario.roles.includes(ADMIN)) {
        $('btnBackup').hidden = false;
        $('btnBackup').onclick = backupBD;
    }
}

async function backupBD() {
    if (!confirm('¿Desea generar la copia de seguridad de la base de datos?')) return;
    await descargar('api/backup', { method: 'POST' });
}

/** Descarga un archivo desde la API. Si el servidor responde con un error, lo muestra sin salir del panel. */
async function descargar(url, opciones = {}) {
    aviso('Generando archivo...');
    try {
        const resp = await fetch(url, { credentials: 'same-origin', ...opciones });
        if (resp.status === 401) { location.replace('index.html'); return; }
        if (!resp.ok) {
            let mensaje = 'Error ' + resp.status;
            try { mensaje = (await resp.json()).error || mensaje; } catch (e) { /* sin cuerpo JSON */ }
            throw new Error(mensaje);
        }
        const blob = await resp.blob();
        const m = /filename="?([^";]+)"?/i.exec(resp.headers.get('Content-Disposition') || '');
        const nombre = m ? m[1] : 'archivo';

        const enlace = document.createElement('a');
        enlace.href = URL.createObjectURL(blob);
        enlace.download = nombre;
        document.body.appendChild(enlace);
        enlace.click();
        enlace.remove();
        setTimeout(() => URL.revokeObjectURL(enlace.href), 10000);
        aviso('Archivo descargado: ' + nombre);
    } catch (err) {
        aviso(err.message, 'err');
    }
}

iniciar().catch(err => aviso(err.message, 'err'));