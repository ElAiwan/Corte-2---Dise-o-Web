function Navbar({ usuario, onLogout }) {
  return (
    <header className="navbar">
      <span className="navbar-titulo">TecnoUAM – Préstamo de equipos</span>
      <div className="navbar-usuario">
        <span>
          {usuario.username} <span className="rol">{usuario.rol}</span>
        </span>
        <button type="button" className="boton boton-secundario" onClick={onLogout}>
          Cerrar sesión
        </button>
      </div>
    </header>
  );
}

export default Navbar;
