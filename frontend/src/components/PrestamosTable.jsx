import { useEffect, useState } from 'react';
import { listarPrestamos } from '../services/prestamoService';
import { mensajeDeError } from '../api/axiosClient';
import EstadoBadge from './EstadoBadge';

function PrestamosTable() {
  const [prestamos, setPrestamos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);

  const cargarPrestamos = async () => {
    setCargando(true);
    setError(null);
    try {
      const data = await listarPrestamos();
      setPrestamos(data);
    } catch (err) {
      setError(mensajeDeError(err));
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    cargarPrestamos();
  }, []);

  let contenido;
  if (cargando) {
    contenido = <p className="estado-mensaje">Cargando préstamos...</p>;
  } else if (error) {
    contenido = (
      <div className="error-caja">
        <span>{error}</span>
        <button type="button" className="boton" onClick={cargarPrestamos}>
          Reintentar
        </button>
      </div>
    );
  } else if (prestamos.length === 0) {
    contenido = <p className="estado-mensaje">No hay préstamos registrados</p>;
  } else {
    contenido = (
      <div className="tabla-scroll">
        <table className="tabla">
          <thead>
            <tr>
              <th>Equipo</th>
              <th>Solicitante</th>
              <th>Préstamo</th>
              <th>Devolución prevista</th>
              <th>Devolución real</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            {prestamos.map((p) => (
              <tr key={p.id}>
                <td>
                  <span className="codigo">{p.equipoCodigo}</span>
                  <br />
                  <small>{p.equipoNombre}</small>
                </td>
                <td>{p.nombreSolicitante}</td>
                <td>{p.fechaPrestamo}</td>
                <td>{p.fechaDevolucionPrevista}</td>
                <td>{p.fechaDevolucionReal ?? '—'}</td>
                <td>
                  <EstadoBadge estado={p.estado} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
  }

  return (
    <section className="tarjeta">
      <div className="tarjeta-encabezado">
        <h2>Préstamos</h2>
        <button type="button" className="boton" onClick={cargarPrestamos} disabled={cargando}>
          Recargar
        </button>
      </div>
      {contenido}
    </section>
  );
}

export default PrestamosTable;
