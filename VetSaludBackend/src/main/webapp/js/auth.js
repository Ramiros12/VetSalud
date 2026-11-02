// Utilidades comunes de autenticación y llamadas a la API (se usan en index.html y dashboard.html)

/** fetch JSON con manejo de errores. Lanza Error(mensaje del servidor). Redirige al login si la sesión expiró. */
async function api(url, opciones = {}) {
    const cfg = { credentials: 'same-origin', headers: {}, ...opciones };
    if (cfg.body !== undefined && typeof cfg.body !== 'string') {
        cfg.body = JSON.stringify(cfg.body);
        cfg.headers['Content-Type'] = 'application/json';
    }
    const resp = await fetch(url, cfg);
    let datos = null;
    try { datos = await resp.json(); } catch (e) { /* respuesta sin cuerpo */ }

    if (resp.status === 401 && !url.endsWith('api/login') && !location.pathname.endsWith('index.html')) {
        location.replace('index.html');
        throw new Error('Sesión expirada');
    }
    if (!resp.ok) {
        throw new Error((datos && datos.error) || ('Error ' + resp.status));
    }
    return datos;
}

function login(username, password) {
    return api('api/login', { method: 'POST', body: { username, password } });
}

/**
 * Cierre de sesión: POST /api/logout. Solo si el servidor responde 200 se vuelve al login.
 * Se usa location.replace para que el botón "Atrás" no regrese al panel.
 */
async function logout() {
    try {
        const resp = await fetch('api/logout', { method: 'POST', credentials: 'same-origin' });
        if (resp.status === 200) {
            location.replace('index.html');
        } else {
            alert('No se pudo cerrar la sesión (código ' + resp.status + ')');
        }
    } catch (err) {
        console.error('Error durante el cierre de sesión:', err);
        alert('No se pudo contactar con el servidor para cerrar la sesión');
    }
}