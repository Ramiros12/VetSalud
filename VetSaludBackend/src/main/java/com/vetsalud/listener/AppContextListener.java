package com.vetsalud.listener;

import com.vetsalud.util.JPAUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/** Cierra el EntityManagerFactory al detener/redesplegar la app (evita fugas de conexiones en Tomcat). */
@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().log("VetSalud iniciado");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JPAUtil.close();
        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.uncheckedShutdown();
        } catch (Throwable ignorado) {
            // no es crítico
        }
    }
}
