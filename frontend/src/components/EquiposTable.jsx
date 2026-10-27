import { useEffect, useState } from 'react';
import { listarEquipos } from '../services/equipoService';
import { mensajeDeError } from '../api/axiosClient';
import EstadoBadge from './EstadoBadge';

function EquiposTable() {
  const [equipos, setEquipos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);

  const cargarEquipos = async () => {
    setCargando(true);
    setError(null);
    try {
      const data = await listarEquipos();
      setEquipos(data);
    } catch (err) {
      setError(mensajeDeError(err));
    } finally {
      setCargando(false);
    }
  };

  // [] → se ejecuta una sola vez, cuando el componente se monta.
  useEffect(() => {
    cargarEquipos();
  }, []);

  const disponibles = equipos.filter((e) => e.estado === 'DISPONIBLE').length;
  const prestados = equipos.filter((e) => e.estado === 'PRESTADO').length;

  let contenido;
  if (cargando) {
    contenido = <p className="estado-mensaje">Cargando equipos...</p>;
  } else if (error) {
    contenido = (
      <div className="error-caja">
        <span>{error}</span>
        <button type="button" className="boton" onClick={cargarEquipos}>
          Reintentar
        </button>
      </div>
    );
  } else if (equipos.length === 0) {
    contenido = <p className="estado-mensaje">No hay equipos registrados</p>;
  } else {
    contenido = (
      <div className="tabla-scroll">
        <table className="tabla">
          <thead>
            <tr>
              <th>Código</th>
              <th>Nombre</th>
              <th>Tipo</th>
              <th>Estado</th>
            </tr>
          </thead>
          <tbody>
            {equipos.map((equipo) => (
              <tr key={equipo.id}>
                <td className="codigo">{equipo.codigoPatrimonial}</td>
                <td>{equipo.nombre}</td>
                <td>{equipo.tipo}</td>
                <td>
                  <EstadoBadge estado={equipo.estado} />
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
        <div>
          <h2>Equipos tecnológicos</h2>
          {!cargando && !error && (
            <p className="contador">
              {equipos.length} equipos · {disponibles} disponibles · {prestados} prestados
            </p>
          )}
        </div>
        <button type="button" className="boton" onClick={cargarEquipos} disabled={cargando}>
          Recargar
        </button>
      </div>
      {contenido}
    </section>
  );
}

export default EquiposTable;
