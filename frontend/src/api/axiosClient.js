import axios from 'axios';

// Instancia única de Axios: todas las peticiones usan la URL base del .env (VITE_API_URL).
const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

// Interceptor de PETICIÓN: si hay token en localStorage, agrega "Authorization: Bearer <token>".
axiosClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor de RESPUESTA: un 401 fuera del login significa token ausente, inválido o expirado.
// Se borra la sesión y se recarga la página para volver al login con el aviso "Sesión expirada".
axiosClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const esLogin = error.config?.url?.includes('/auth/login');
    if (error.response?.status === 401 && !esLogin) {
      localStorage.removeItem('token');
      localStorage.removeItem('username');
      localStorage.removeItem('rol');
      sessionStorage.setItem('sesionExpirada', 'true');
      window.location.reload();
    }
    // Siempre se re-lanza el error para que el componente pueda mostrarlo.
    return Promise.reject(error);
  }
);

// Traduce un error de Axios a un mensaje legible para la interfaz.
export function mensajeDeError(err) {
  if (!err.response) {
    return 'No se pudo conectar con el servidor';
  }
  const { status, data } = err.response;
  if (status === 401) return 'Sesión expirada, inicia sesión de nuevo';
  if (status === 403) return 'No tienes permisos para ver esta información';
  return data?.message || `Error inesperado (${status})`;
}

export default axiosClient;
