// Etiqueta de color según el estado: rojo si el equipo está fuera (PRESTADO / préstamo ACTIVO),
// verde si está disponible (DISPONIBLE / préstamo DEVUELTO).
function EstadoBadge({ estado }) {
  const clase = estado === 'PRESTADO' || estado === 'ACTIVO' ? 'badge badge-rojo' : 'badge badge-verde';
  return <span className={clase}>{estado}</span>;
}

export default EstadoBadge;
