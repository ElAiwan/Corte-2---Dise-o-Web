import { useState } from 'react';
import { login } from '../services/authService';

function LoginForm({ onLogin }) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState(null);

  // Aviso que deja el interceptor de Axios cuando el token expiró (se lee una sola vez).
  const [aviso] = useState(() => {
    const expirada = sessionStorage.getItem('sesionExpirada');
    sessionStorage.removeItem('sesionExpirada');
    return expirada ? 'Sesión expirada, inicia sesión de nuevo' : null;
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setCargando(true);
    setError(null);
    try {
      const data = await login(username, password);
      onLogin({ username: data.username, rol: data.rol });
    } catch (err) {
      if (!err.response) {
        setError('No se pudo conectar con el servidor (¿está encendido el backend en el puerto 8080?)');
      } else if (err.response.status === 401) {
        setError('Usuario o contraseña incorrectos');
      } else {
        setError(err.response.data?.message || 'Error al iniciar sesión');
      }
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="login-pagina">
      <form className="login-caja" onSubmit={handleSubmit}>
        <h1>TecnoUAM</h1>
        <p className="subtitulo">Préstamo de equipos tecnológicos</p>

        {aviso && !error && <div className="aviso">{aviso}</div>}
        {error && <div className="error-caja">{error}</div>}

        <label htmlFor="username">Usuario</label>
        <input
          id="username"
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          autoComplete="username"
          required
        />

        <label htmlFor="password">Contraseña</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          autoComplete="current-password"
          required
        />

        <button type="submit" className="boton" disabled={cargando}>
          {cargando ? 'Ingresando...' : 'Iniciar sesión'}
        </button>
      </form>
    </div>
  );
}

export default LoginForm;
