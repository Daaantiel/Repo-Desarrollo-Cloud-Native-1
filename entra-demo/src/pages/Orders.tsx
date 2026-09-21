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

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '40px' }}>
        <h3>⏳ Cargando tus pedidos...</h3>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ textAlign: 'center', padding: '40px', color: '#c62828' }}>
        <h3> Error al consultar pedidos</h3>
        <p style={{ background: '#ffebee', padding: '10px', borderRadius: '6px', display: 'inline-block' }}>
          {error}
        </p>
        <br />
        <button
          onClick={loadOrders}
          style={{
            marginTop: '15px',
            padding: '8px 16px',
            backgroundColor: '#2a75bb',
            color: '#fff',
            border: 'none',
            borderRadius: '6px',
            cursor: 'pointer',
            fontWeight: 'bold'
          }}
        >
          Reintentar
        </button>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '1000px', margin: '0 auto', padding: '20px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h2 style={{ color: '#2a75bb', margin: 0 }}>📦 Mis Pedidos de Cartas</h2>
        <button
          onClick={loadOrders}
          style={{
            padding: '8px 14px',
            backgroundColor: '#ffffff',
            border: '1px solid #2a75bb',
            color: '#2a75bb',
            borderRadius: '6px',
            cursor: 'pointer',
            fontWeight: 'bold'
          }}
        >
           Actualizar Tabla
        </button>
      </div>

      {orders.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '40px', backgroundColor: '#f9f9f9', borderRadius: '8px' }}>
          <p style={{ fontSize: '1.1rem', color: '#666' }}>Aún no has realizado ninguna compra de cartas Pokémon.</p>
        </div>
      ) : (
        <div style={{ overflowX: 'auto', boxShadow: '0 2px 8px rgba(0,0,0,0.1)', borderRadius: '8px' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', backgroundColor: '#fff' }}>
            <thead>
              <tr style={{ backgroundColor: '#2a75bb', color: '#fff', textAlign: 'left' }}>
                <th style={{ padding: '12px' }}>ID Pedido</th>
                <th style={{ padding: '12px' }}>Producto ID</th>
                <th style={{ padding: '12px' }}>Cantidad</th>
                <th style={{ padding: '12px' }}>Estado</th>
                <th style={{ padding: '12px' }}>Fecha</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id} style={{ borderBottom: '1px solid #eee' }}>
                  <td style={{ padding: '12px', fontWeight: 'bold' }}>#{order.id}</td>
                  <td style={{ padding: '12px' }}>
                    {order.nombreProducto || `Carta #${order.productoId}`}
                  </td>
                  <td style={{ padding: '12px' }}>{order.cantidad} un.</td>
                  <td style={{ padding: '12px' }}>
                    <OrderStatusBadge status={order.estado} />
                  </td>
                  <td style={{ padding: '12px', fontSize: '0.85rem', color: '#666' }}>
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
  );
};