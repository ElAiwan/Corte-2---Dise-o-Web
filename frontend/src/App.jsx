import { useState } from 'react';
import { getUsuarioActual, logout } from './services/authService';
import LoginForm from './components/LoginForm';
import Navbar from './components/Navbar';
import EquiposTable from './components/EquiposTable';
import PrestamosTable from './components/PrestamosTable';

function App() {
  // Se inicializa desde localStorage: así la sesión sobrevive al F5 y la tabla vuelve a pedir datos.
  const [usuario, setUsuario] = useState(getUsuarioActual());

  const handleLogout = () => {
    logout();
    setUsuario(null);
  };

  if (!usuario) {
    return <LoginForm onLogin={setUsuario} />;
  }

  return (
    <>
      <Navbar usuario={usuario} onLogout={handleLogout} />
      <main className="contenedor">
        <EquiposTable />
        <PrestamosTable />
      </main>
    </>
  );
}

export default App;
