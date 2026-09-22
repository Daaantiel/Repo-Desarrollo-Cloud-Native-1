import React, { useEffect, useState } from 'react';
import { useMsal } from '@azure/msal-react';
import { OrderStatusBadge, type OrderStatus } from '../components/OrderStatusBadge';
import { tokenRequest } from '../authConfig';

interface OrderItem {
  id: number;
  productoId: number;
  nombreProducto?: string;
  cantidad: number;
  precioUnitario?: number;
  precioTotal?: number;
  estado: OrderStatus | string;
  fechaCreacion?: string;
}

export const Orders: React.FC = () => {
  const { instance, accounts } = useMsal();
  const [orders, setOrders] = useState<OrderItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const BFF_URL = import.meta.env.VITE_BFF_BASE_URL || 'http://3.83.25.148:8081';

  // Token JWT de Entra ID
  const getAccessToken = async (): Promise<string | null> => {
    if (accounts.length === 0) return null;
    try {
      const response = await instance.acquireTokenSilent({
        ...tokenRequest,
        account: accounts[0]
      });
      return response.accessToken;
    } catch (e) {
      console.error('Error al obtener token:', e);
      return null;
    }
  };

  // Cargar pedidos desde el BFF
  const loadOrders = async () => {
    setLoading(true);
    setError(null);
    try {
      const token = await getAccessToken();
      const response = await fetch(`${BFF_URL}/api/orders`, {
        headers: {
          'Authorization': token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: No se pudo obtener la lista de pedidos`);
      }

      const data = await response.json();
      setOrders(data);
    } catch (err: any) {
      setError(err.message || 'Error de conexión con ms-pedidos');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOrders();
  }, [accounts]);

  // Vista de Carga Estilizada
  if (loading) {
    return (
      <div style={{
        minHeight: 'calc(100vh - 70px)',
        background: 'radial-gradient(circle at 50% -20%, #1e1b4b 0%, #0f172a 60%, #020617 100%)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '40px',
        color: '#f8fafc'
      }}>
        <div style={{ fontSize: '3rem', marginBottom: '12px' }}></div>
        <h3 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 800, color: '#fbbf24' }}>
          Cargando tus pedidos...
        </h3>
        <p style={{ color: '#94a3b8', fontSize: '0.85rem', marginTop: '6px' }}>
          Consultando el historial de transacciones desde ms-pedidos
        </p>
      </div>
    );
  }

  // Vista de Error Estilizada
  if (error) {
    return (
      <div style={{
        minHeight: 'calc(100vh - 70px)',
        background: 'radial-gradient(circle at 50% -20%, #1e1b4b 0%, #0f172a 60%, #020617 100%)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '40px'
      }}>
        <div style={{
          backgroundColor: 'rgba(239, 68, 68, 0.08)',
          border: '1px solid rgba(239, 68, 68, 0.25)',
          padding: '32px',
          borderRadius: '20px',
          textAlign: 'center',
          maxWidth: '450px',
          backdropFilter: 'blur(10px)'
        }}>
          <span style={{ fontSize: '2.5rem' }}></span>
          <h3 style={{ color: '#f87171', margin: '12px 0 8px 0', fontWeight: 800 }}>
            Error al consultar pedidos
          </h3>
          <p style={{ color: '#fca5a5', fontSize: '0.88rem', margin: '0 0 20px 0' }}>
            {error}
          </p>
          <button
            onClick={loadOrders}
            style={{
              padding: '10px 20px',
              backgroundColor: '#ef4444',
              color: '#ffffff',
              border: 'none',
              borderRadius: '10px',
              cursor: 'pointer',
              fontWeight: '800',
              fontSize: '0.9rem',
              boxShadow: '0 4px 12px rgba(239, 68, 68, 0.3)'
            }}
          >
            Reintentar
          </button>
        </div>
      </div>
    );
  }

  return (
    <div style={{
      minHeight: 'calc(100vh - 70px)',
      background: 'radial-gradient(circle at 50% -20%, #1e1b4b 0%, #0f172a 60%, #020617 100%)',
      padding: '32px 20px 60px 20px',
      boxSizing: 'border-box'
    }}>
      <div style={{ maxWidth: '1000px', margin: '0 auto' }}>
        
        {/* Header */}
        <div style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '32px',
          flexWrap: 'wrap',
          gap: '16px'
        }}>
          <div>
            <h2 style={{
              margin: 0,
              fontSize: '1.8rem',
              fontWeight: 900,
              color: '#f8fafc',
              display: 'flex',
              alignItems: 'center',
              gap: '10px'
            }}>
              <span></span> Mis Pedidos de Cartas
            </h2>
            <p style={{ margin: '4px 0 0 0', color: '#94a3b8', fontSize: '0.88rem' }}>
              Historial de compras y estado de tus transacciones
            </p>
          </div>

          <button
            onClick={loadOrders}
            style={{
              padding: '10px 18px',
              backgroundColor: 'rgba(255, 255, 255, 0.06)',
              border: '1px solid rgba(255, 255, 255, 0.15)',
              color: '#fbbf24',
              borderRadius: '12px',
              cursor: 'pointer',
              fontWeight: '700',
              fontSize: '0.88rem',
              backdropFilter: 'blur(10px)',
              display: 'flex',
              alignItems: 'center',
              gap: '6px'
            }}
          >
            Actualizar Tabla
          </button>
        </div>

        {/* Estado Vacío / Tabla de Pedidos */}
        {orders.length === 0 ? (
          <div style={{
            textAlign: 'center',
            padding: '60px 20px',
            backgroundColor: 'rgba(255, 255, 255, 0.03)',
            borderRadius: '20px',
            border: '1px solid rgba(255, 255, 255, 0.08)',
            color: '#94a3b8'
          }}>
            <span style={{ fontSize: '2.5rem', display: 'block', marginBottom: '12px' }}>🃏</span>
            <p style={{ margin: 0, fontSize: '1rem', fontWeight: 600, color: '#f8fafc' }}>
              Aún no has realizado ninguna compra de cartas Pokémon.
            </p>
            <p style={{ margin: '6px 0 0 0', fontSize: '0.85rem' }}>
              Visita el catálogo para realizar tu primer pedido.
            </p>
          </div>
        ) : (
          <div style={{
            overflowX: 'auto',
            borderRadius: '18px',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            backgroundColor: 'rgba(15, 23, 42, 0.65)',
            backdropFilter: 'blur(12px)',
            boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.3)'
          }}>
            <table style={{
              width: '100%',
              borderCollapse: 'collapse',
              color: '#f8fafc',
              textAlign: 'left'
            }}>
              <thead>
                <tr style={{
                  borderBottom: '1px solid rgba(255, 255, 255, 0.12)',
                  backgroundColor: 'rgba(255, 255, 255, 0.04)'
                }}>
                  <th style={{ padding: '16px 20px', fontSize: '0.82rem', textTransform: 'uppercase', color: '#fbbf24', letterSpacing: '0.5px' }}>
                    ID Pedido
                  </th>
                  <th style={{ padding: '16px 20px', fontSize: '0.82rem', textTransform: 'uppercase', color: '#fbbf24', letterSpacing: '0.5px' }}>
                    Producto / Carta
                  </th>
                  <th style={{ padding: '16px 20px', fontSize: '0.82rem', textTransform: 'uppercase', color: '#fbbf24', letterSpacing: '0.5px' }}>
                    Cantidad
                  </th>
                  <th style={{ padding: '16px 20px', fontSize: '0.82rem', textTransform: 'uppercase', color: '#fbbf24', letterSpacing: '0.5px' }}>
                    Estado
                  </th>
                  <th style={{ padding: '16px 20px', fontSize: '0.82rem', textTransform: 'uppercase', color: '#fbbf24', letterSpacing: '0.5px' }}>
                    Fecha
                  </th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order, index) => (
                  <tr
                    key={order.id}
                    style={{
                      borderBottom: index === orders.length - 1 ? 'none' : '1px solid rgba(255, 255, 255, 0.06)'
                    }}
                  >
                    <td style={{ padding: '16px 20px', fontWeight: 800, fontSize: '0.9rem', color: '#cbd5e1' }}>
                      <span style={{
                        backgroundColor: 'rgba(255, 255, 255, 0.08)',
                        padding: '4px 10px',
                        borderRadius: '8px',
                        fontFamily: 'monospace'
                      }}>
                        #{order.id}
                      </span>
                    </td>
                    <td style={{ padding: '16px 20px', fontWeight: 700, fontSize: '0.95rem', color: '#ffffff' }}>
                      {order.nombreProducto || `Carta #${order.productoId}`}
                    </td>
                    <td style={{ padding: '16px 20px', fontWeight: 600, color: '#94a3b8', fontSize: '0.9rem' }}>
                      {order.cantidad} un.
                    </td>
                    <td style={{ padding: '16px 20px' }}>
                      <OrderStatusBadge status={order.estado} />
                    </td>
                    <td style={{ padding: '16px 20px', fontSize: '0.85rem', color: '#94a3b8' }}>
                      {order.fechaCreacion 
                        ? new Date(order.fechaCreacion).toLocaleString('es-CL') 
                        : 'Reciente'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

      </div>
    </div>
  );
};