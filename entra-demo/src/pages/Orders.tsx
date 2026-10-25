import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useMsal } from '@azure/msal-react';
import { tokenRequest } from '../authConfig';

interface Pedido {
  id: number;
  productoId?: number;
  productId?: number;
  nombreProducto?: string;
  cantidad?: number;
  quantity?: number;
  estado?: string;
  fecha?: string;
  fechaCreacion?: string;
}

interface ResumenProducto {
  productoId: number;
  nombreProducto: string;
  cantidadTotal: number;
  totalPedidos: number;
}

const BFF_URL =
  import.meta.env.VITE_BFF_BASE_URL || 'http://localhost:8081';

const fondo =
  'radial-gradient(circle at 50% -20%, #1e1b4b 0%, #0f172a 60%, #020617 100%)';

const estiloBoton: React.CSSProperties = {
  padding: '11px 17px',
  borderRadius: '11px',
  border: '1px solid rgba(255,255,255,0.14)',
  background: 'rgba(255,255,255,0.07)',
  color: '#fbbf24',
  cursor: 'pointer',
  fontWeight: 700,
};

const estiloCelda: React.CSSProperties = {
  padding: '17px 20px',
  borderBottom: '1px solid rgba(255,255,255,0.06)',
};

export const Orders: React.FC = () => {
  const { instance, accounts } = useMsal();

  const [orders, setOrders] = useState<Pedido[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [mostrarHistorial, setMostrarHistorial] = useState(false);

  // Obtener los pedidos mediante el BFF protegido por Entra ID.
  const loadOrders = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const account = accounts[0];

      if (!account) {
        throw new Error('Debes iniciar sesión para consultar los pedidos.');
      }

      const tokenResponse = await instance.acquireTokenSilent({
        ...tokenRequest,
        account,
      });

      const response = await fetch(`${BFF_URL}/api/orders`, {
        headers: {
          Authorization: `Bearer ${tokenResponse.accessToken}`,
          'Content-Type': 'application/json',
        },
      });

      if (!response.ok) {
        throw new Error(
          `Error HTTP ${response.status} al consultar los pedidos`
        );
      }

      const data: unknown = await response.json();

      if (!Array.isArray(data)) {
        throw new Error('El servidor devolvió un formato inesperado.');
      }

      setOrders(data as Pedido[]);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : 'Error al cargar los pedidos.'
      );
    } finally {
      setLoading(false);
    }
  }, [instance, accounts]);

  useEffect(() => {
    void loadOrders();
  }, [loadOrders]);

  // Agrupar compras de una misma carta y sumar sus cantidades.
  const resumen = useMemo<ResumenProducto[]>(() => {
    const agrupados = new Map<number, ResumenProducto>();

    for (const pedido of orders) {
      if (!['PAGADO', 'COMPLETADO'].includes(String(pedido.estado ?? '').toUpperCase())) {
        continue;
      }

      const productoId = Number(
        pedido.productoId ?? pedido.productId
      );

      const cantidad = Number(
        pedido.cantidad ?? pedido.quantity ?? 1
      );

      if (
        !Number.isInteger(productoId) ||
        productoId <= 0 ||
        !Number.isInteger(cantidad) ||
        cantidad <= 0
      ) {
        continue;
      }

      const actual = agrupados.get(productoId);

      if (actual) {
        actual.cantidadTotal += cantidad;
        actual.totalPedidos += 1;
      } else {
        agrupados.set(productoId, {
          productoId,
          nombreProducto:
            pedido.nombreProducto || `Carta #${productoId}`,
          cantidadTotal: cantidad,
          totalPedidos: 1,
        });
      }
    }

    return Array.from(agrupados.values()).sort(
      (a, b) => a.productoId - b.productoId
    );
  }, [orders]);

  const totalCartas = resumen.reduce(
    (total, item) => total + item.cantidadTotal,
    0
  );

  const totalCompras = resumen.reduce(
    (total, item) => total + item.totalPedidos,
    0
  );

  return (
    <div
      style={{
        minHeight: 'calc(100vh - 70px)',
        background: fondo,
        padding: '32px 20px 60px',
        color: '#f8fafc',
        boxSizing: 'border-box',
      }}
    >
      <div style={{ maxWidth: '1050px', margin: '0 auto' }}>

        {/* ENCABEZADO */}
        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: '18px',
            marginBottom: '26px',
          }}
        >
          <div>
            <h2
              style={{
                fontSize: '1.8rem',
                fontWeight: 900,
                margin: 0,
              }}
            >
              Mis Cartas Compradas
            </h2>

            <p style={{ color: '#94a3b8', marginTop: '7px' }}>
              Resumen acumulado de compras registradas en PostgreSQL
            </p>
          </div>

          <button style={estiloBoton} onClick={() => void loadOrders()}>
            Actualizar Tabla
          </button>
        </div>

        {/* AVISO DE ALCANCE ACTUAL */}
        <p
          style={{
            color: '#94a3b8',
            fontSize: '0.78rem',
            marginBottom: '18px',
          }}
        >
          Pedidos asociados a tu identidad Microsoft Entra ID. Sólo se cuentan cartas pagadas.
        </p>

        {loading ? (
          <div style={{ padding: '65px', textAlign: 'center' }}>
            <h3 style={{ color: '#fbbf24' }}>
              Cargando pedidos desde AWS...
            </h3>
          </div>
        ) : error ? (
          <div
            style={{
              padding: '30px',
              borderRadius: '15px',
              background: 'rgba(239,68,68,0.10)',
              border: '1px solid rgba(239,68,68,0.3)',
            }}
          >
            <h3 style={{ color: '#f87171' }}>
              Error al consultar pedidos
            </h3>
            <p>{error}</p>
            <button style={estiloBoton} onClick={() => void loadOrders()}>
              Reintentar
            </button>
          </div>
        ) : (
          <>
            {/* INDICADORES ACUMULADOS */}
            <div
              style={{
                display: 'grid',
                gridTemplateColumns:
                  'repeat(auto-fit, minmax(190px, 1fr))',
                gap: '15px',
                marginBottom: '26px',
              }}
            >
              {[
                ['Cartas compradas', totalCartas],
                ['Tipos de cartas', resumen.length],
                ['Compras realizadas', totalCompras],
              ].map(([titulo, valor]) => (
                <div
                  key={String(titulo)}
                  style={{
                    background: 'rgba(255,255,255,0.05)',
                    border: '1px solid rgba(255,255,255,0.09)',
                    borderRadius: '16px',
                    padding: '22px',
                  }}
                >
                  <div
                    style={{
                      color: '#94a3b8',
                      fontSize: '0.85rem',
                      marginBottom: '12px',
                    }}
                  >
                    {titulo}
                  </div>
                  <div
                    style={{
                      color: '#fbbf24',
                      fontWeight: 900,
                      fontSize: '2rem',
                    }}
                  >
                    {valor}
                  </div>
                </div>
              ))}
            </div>

            {/* ALTERNAR ENTRE RESUMEN E HISTORIAL */}
            <div
              style={{
                display: 'flex',
                gap: '10px',
                marginBottom: '20px',
              }}
            >
              <button
                style={{
                  ...estiloBoton,
                  background: !mostrarHistorial
                    ? '#f59e0b'
                    : 'rgba(255,255,255,0.07)',
                  color: !mostrarHistorial ? '#111827' : '#fbbf24',
                }}
                onClick={() => setMostrarHistorial(false)}
              >
                Resumen acumulado
              </button>

              <button
                style={{
                  ...estiloBoton,
                  background: mostrarHistorial
                    ? '#f59e0b'
                    : 'rgba(255,255,255,0.07)',
                  color: mostrarHistorial ? '#111827' : '#fbbf24',
                }}
                onClick={() => setMostrarHistorial(true)}
              >
                Historial de pedidos
              </button>
            </div>

            {orders.length === 0 ? (
              <div
                style={{
                  padding: '60px 20px',
                  textAlign: 'center',
                  borderRadius: '18px',
                  background: 'rgba(255,255,255,0.04)',
                }}
              >
                <div style={{ fontSize: '2.5rem' }}></div>
                <h3>Todavía no hay compras registradas.</h3>
                <p style={{ color: '#94a3b8' }}>
                  Visita el catálogo para comprar tu primera carta.
                </p>
              </div>
            ) : (
              <div
                style={{
                  overflowX: 'auto',
                  borderRadius: '18px',
                  border: '1px solid rgba(255,255,255,0.1)',
                  background: 'rgba(15,23,42,0.70)',
                }}
              >
                <table
                  style={{
                    width: '100%',
                    borderCollapse: 'collapse',
                    textAlign: 'left',
                  }}
                >
                  <thead
                    style={{
                      color: '#fbbf24',
                      background: 'rgba(255,255,255,0.05)',
                      fontSize: '0.82rem',
                      textTransform: 'uppercase',
                    }}
                  >
                    {mostrarHistorial ? (
                      <tr>
                        <th style={estiloCelda}>Pedido</th>
                        <th style={estiloCelda}>Carta</th>
                        <th style={estiloCelda}>Cantidad</th>
                        <th style={estiloCelda}>Estado</th>
                        <th style={estiloCelda}>Fecha</th>
                      </tr>
                    ) : (
                      <tr>
                        <th style={estiloCelda}>Producto / Carta</th>
                        <th style={estiloCelda}>Cantidad total</th>
                        <th style={estiloCelda}>Compras</th>
                      </tr>
                    )}
                  </thead>

                  <tbody>
                    {mostrarHistorial
                      ? orders.map((pedido) => (
                          <tr key={`${pedido.id}-${pedido.productoId ?? pedido.productId}`}>
                            <td style={estiloCelda}>#{pedido.id}</td>
                            <td style={estiloCelda}>
                              {pedido.nombreProducto ||
                                `Carta #${pedido.productoId ?? pedido.productId}`}
                            </td>
                            <td style={estiloCelda}>
                              {pedido.cantidad ?? pedido.quantity ?? 1} un.
                            </td>
                            <td style={estiloCelda}>
                              {pedido.estado || 'REGISTRADO'}
                            </td>
                            <td style={estiloCelda}>
                              {pedido.fecha ||
                                pedido.fechaCreacion ||
                                'Reciente'}
                            </td>
                          </tr>
                        ))
                      : resumen.map((item) => (
                          <tr key={item.productoId}>
                            <td
                              style={{
                                ...estiloCelda,
                                fontWeight: 700,
                              }}
                            >
                              {item.nombreProducto}
                            </td>
                            <td
                              style={{
                                ...estiloCelda,
                                color: '#34d399',
                                fontWeight: 900,
                              }}
                            >
                              {item.cantidadTotal} un.
                            </td>
                            <td style={estiloCelda}>
                              {item.totalPedidos}
                            </td>
                          </tr>
                        ))}
                  </tbody>
                </table>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};
