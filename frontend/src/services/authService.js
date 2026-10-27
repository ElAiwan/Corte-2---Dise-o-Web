import axiosClient from '../api/axiosClient';

// POST /auth/login → guarda token, username y rol en localStorage (claves del contrato C6).
export async function login(username, password) {
  const response = await axiosClient.post('/auth/login', { username, password });
  const { token, username: user, rol } = response.data;
  localStorage.setItem('token', token);
  localStorage.setItem('username', user);
  localStorage.setItem('rol', rol);
  return response.data;
}

export function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('username');
  localStorage.removeItem('rol');
}

// Devuelve { username, rol } si hay sesión guardada; si no, null.
export function getUsuarioActual() {
  const token = localStorage.getItem('token');
  if (!token) return null;
  return {
    username: localStorage.getItem('username'),
    rol: localStorage.getItem('rol'),
  };
}
