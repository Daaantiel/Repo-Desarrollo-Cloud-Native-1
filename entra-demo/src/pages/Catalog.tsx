import React, { useEffect, useState } from 'react';
import { useMsal } from '@azure/msal-react';
import { PokemonCard, type PokemonProduct } from '../components/PokemonCard';
import { tokenRequest } from '../authConfig';

export const Catalog: React.FC = () => {
  const { instance, accounts } = useMsal();
  const [products, setProducts] = useState<PokemonProduct[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [cart, setCart] = useState<{ product: PokemonProduct; cantidad: number }[]>([]);
  const [pendingOrder, setPendingOrder] = useState<number | null>(null);
  const [processing, setProcessing] = useState(false);
  const [feedback, setFeedback] = useState('');

  const BFF_URL = import.meta.env.VITE_BFF_BASE_URL || 'http://localhost:8081';

  // 1. Obtención del token JWT de Entra ID
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

  // 2. Consulta de productos al BFF
  const loadProducts = async () => {
    setLoading(true);
    setError(null);
    try {
      const token = await getAccessToken();
      const response = await fetch(`${BFF_URL}/api/catalog/products`, {
        headers: {
          'Authorization': token ? `Bearer ${token}` : '',
          'Content-Type': 'application/json'
        }
      });

      if (!response.ok) {
        throw new Error(`Error ${response.status}: ${response.statusText}`);
      }

      const data = await response.json();
      setProducts(data);
    } catch (err: any) {
      setError(err.message || 'Error al conectar con el servidor BFF');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, [accounts]);

  // Agregar al carrito sin generar todavía un pedido.
  const handleCreateOrder = (product: PokemonProduct) => {
    if (pendingOrder !== null) {
      alert('Resuelve primero el pago de prueba pendiente.');
      return;
    }
    if (Number(product.stock) <= 0) {
      setFeedback(`La carta "${product.nombre}" está agotada.`);
      return;
    }
    setCart(prev => {
      const actual = prev.find(item => item.product.id === product.id);
      if (actual && actual.cantidad >= Number(product.stock)) return prev;
      if (actual) {
        return prev.map(item => item.product.id === product.id
          ? { ...item, cantidad: Math.min(Number(product.stock), 99, item.cantidad + 1) }
          : item);
      }
      return [...prev, { product, cantidad: 1 }];
    });
    setFeedback(`Carta "${product.nombre}" agregada al carrito`);
  };

  const cambiarCantidad = (id: number, delta: number) => {
    if (pendingOrder !== null) return;
    setCart(prev => prev
      .map(item => item.product.id === id
        ? { ...item, cantidad: Math.min(Number(item.product.stock), item.cantidad + delta) }
        : item)
      .filter(item => item.cantidad > 0));
  };

  const totalCarrito = cart.reduce(
    (total, item) => total + Number(item.product.precio || 0) * item.cantidad,
    0
  );

  // Crea un pedido PENDIENTE_PAGO vinculado por el BFF al usuario JWT.
  const finalizarCompra = async () => {
    if (cart.length === 0 || processing || pendingOrder !== null) return;
    setProcessing(true);
    try {
      const token = await getAccessToken();
      if (!token) throw new Error('Sesión de Microsoft no disponible');
      const response = await fetch(`${BFF_URL}/api/orders`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          items: cart.map(item => ({
            productoId: item.product.id,
            cantidad: item.cantidad,
            precioUnitario: item.product.precio,
          })),
        }),
      });
      if (!response.ok) throw new Error(`Error ${response.status} al registrar pedido`);
      const data = await response.json();
      if (!Number.isInteger(Number(data.id))) throw new Error('Pedido sin ID');
      setPendingOrder(Number(data.id));
      setFeedback(`Pedido #${data.id} creado: PENDIENTE_PAGO`);
    } catch (err) {
      setFeedback(err instanceof Error ? err.message : 'Error creando pedido');
    } finally {
      setProcessing(false);
    }
  };

  const simularPago = async (approved: boolean) => {
    if (pendingOrder === null || processing) return;
    setProcessing(true);
    try {
      const token = await getAccessToken();
      if (!token) throw new Error('Sesión de Microsoft no disponible');
      const response = await fetch(
        `${BFF_URL}/api/orders/${pendingOrder}/simulate-payment`, {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ approved }),
        }
      );
      if (!response.ok) throw new Error(`Error ${response.status} al simular pago`);
      const data = await response.json();
      setFeedback(approved
        ? `Pago DEMO aprobado. Pedido #${data.id}: ${data.estado}. RabbitMQ: ${data.mensajeria ?? 'pendiente'}`
        : `Pago DEMO rechazado. Pedido #${data.id}: ${data.estado}`);
      setPendingOrder(null);
      setCart([]);
      if (approved) await loadProducts();
    } catch (err) {
      setFeedback(err instanceof Error ? err.message : 'Error simulando pago');
    } finally {
      setProcessing(false);
    }
  };

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
          Cargando Pokédex desde AWS...
        </h3>
        <p style={{ color: '#94a3b8', fontSize: '0.85rem', marginTop: '6px' }}>
          Obteniendo cartas disponibles desde el BFF
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
            Error al consultar el catálogo
          </h3>
          <p style={{ color: '#fca5a5', fontSize: '0.88rem', margin: '0 0 20px 0' }}>
            {error}
          </p>
          <button
            onClick={loadProducts}
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
            Reintentar Carga
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
      <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
        
        {/* Header del Catálogo */}
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
            <span></span> Catálogo de Cartas Pokémon
            </h2>
            <p style={{ margin: '4px 0 0 0', color: '#94a3b8', fontSize: '0.88rem' }}>
              Colección oficial disponible para pedidos en tiempo real
            </p>
          </div>

          <button
            onClick={loadProducts}
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
              gap: '6px',
              transition: 'all 0.2s ease'
            }}
          >
            Actualizar
          </button>
        </div>

        {/* Carrito y pasarela simulada, sin dinero real */}
        <div style={{
          background: 'rgba(255,255,255,0.05)',
          border: '1px solid rgba(255,255,255,0.12)',
          borderRadius: '18px', padding: '22px', marginBottom: '24px',
          color: '#f8fafc'
        }}>
          <h3 style={{ marginTop: 0, color: '#fbbf24' }}>Carrito de compras (DEMO)</h3>
          {cart.length === 0 ? (
            <p style={{color:'#94a3b8'}}>Selecciona una carta para agregarla al carrito.</p>
          ) : (
            <>
              {cart.map(item => (
                <div key={item.product.id} style={{
                  display: 'flex', alignItems: 'center',
                  justifyContent: 'space-between', gap: '12px',
                  borderBottom: '1px solid rgba(255,255,255,0.08)',
                  padding: '10px 0', flexWrap: 'wrap'
                }}>
                  <strong>{item.product.nombre}</strong>
                  <div style={{display:'flex',alignItems:'center',gap:'10px'}}>
                    <button disabled={processing || pendingOrder !== null}
                      onClick={() => cambiarCantidad(item.product.id, -1)}>-</button>
                    <span>{item.cantidad} un.</span>
                    <button disabled={processing || pendingOrder !== null}
                      onClick={() => cambiarCantidad(item.product.id, 1)}>+</button>
                  </div>
                  <strong>${(Number(item.product.precio || 0) * item.cantidad).toLocaleString('es-CL')}</strong>
                </div>
              ))}
              <p style={{ textAlign: 'right', fontWeight: 800, color: '#fbbf24' }}>
                Total ilustrativo: ${totalCarrito.toLocaleString('es-CL')}
              </p>
            </>
          )}
          {feedback && <p role="status" style={{ color: '#34d399' }}>{feedback}</p>}
          {pendingOrder === null ? (
            <button
              disabled={processing || cart.length === 0}
              onClick={finalizarCompra}
              style={{padding:'12px 20px', background:'#f59e0b', color:'#111827',
                border:0, borderRadius:'10px', fontWeight:800,
                cursor:'pointer', opacity: cart.length === 0 ? 0.5 : 1}}
            >Finalizar compra</button>
          ) : (
            <div style={{border:'1px solid #fbbf24', borderRadius:'12px', padding:'15px'}}>
              <h4 style={{margin:'0 0 6px'}}>Pago simulado — Pedido #{pendingOrder}</h4>
              <p style={{fontSize:'0.85rem',color:'#94a3b8'}}>
                No ingreses datos bancarios. No se realizará ningún cobro real.
              </p>
              <div style={{display:'flex',gap:'10px',flexWrap:'wrap'}}>
                <button disabled={processing} onClick={() => simularPago(true)}
                  style={{padding:'11px 16px',background:'#10b981',color:'#061a16',
                    border:0,borderRadius:'9px',fontWeight:800}}>Aprobar pago DEMO</button>
                <button disabled={processing} onClick={() => simularPago(false)}
                  style={{padding:'11px 16px',background:'#ef4444',color:'white',
                    border:0,borderRadius:'9px',fontWeight:800}}>Rechazar pago DEMO</button>
              </div>
            </div>
          )}
        </div>

        {/* Rejilla de Cartas */}
        {products.length === 0 ? (
          <div style={{
            textAlign: 'center',
            padding: '60px 20px',
            backgroundColor: 'rgba(255, 255, 255, 0.03)',
            borderRadius: '20px',
            border: '1px solid rgba(255, 255, 255, 0.08)',
            color: '#94a3b8'
          }}>
            <p style={{ margin: 0, fontSize: '1rem', fontWeight: 600 }}>
              No hay cartas Pokémon registradas en la base de datos.
            </p>
          </div>
        ) : (
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(230px, 1fr))',
              gap: '24px',
              justifyItems: 'center'
            }}
          >
            {products.map((product) => (
              <PokemonCard
                key={product.id}
                product={product}
                onOrder={handleCreateOrder}
                isOrdering={processing}
              />
            ))}
          </div>
        )}

      </div>
    </div>
  );
};