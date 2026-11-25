import React from 'react';

export interface PokemonProduct {
  id: number;
  nombre: string;
  tipo?: string;
  precio: number;
  stock: number;
  imagenUrl?: string;
}

interface PokemonCardProps {
  product: PokemonProduct;
  onOrder: (product: PokemonProduct) => void;
  isOrdering?: boolean;
}

export const PokemonCard: React.FC<PokemonCardProps> = ({
  product,
  onOrder,
  isOrdering = false
}) => {
  const imageUrl =
    product.imagenUrl ||
    `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/${product.id}.png`;

  const hasStock = product.stock > 0;

  return (
    <div
      style={{
        border: '1px solid #e2e8f0',
        borderRadius: '18px',
        padding: '16px',
        width: '230px',
        backgroundColor: '#ffffff',
        boxShadow: '0 10px 25px -5px rgba(15, 23, 42, 0.08), 0 8px 10px -6px rgba(15, 23, 42, 0.04)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: '12px',
        position: 'relative'
      }}
    >
     
      <div
        style={{
          backgroundColor: '#f8fafc',
          borderRadius: '14px',
          padding: '12px',
          width: '100%',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          position: 'relative',
          boxSizing: 'border-box',
          minHeight: '140px',
          border: '1px solid #f1f5f9'
        }}
      >
        
        <span
          style={{
            position: 'absolute',
            top: '8px',
            right: '8px',
            backgroundColor: hasStock ? '#ecfdf5' : '#fef2f2',
            color: hasStock ? '#047857' : '#dc2626',
            border: `1px solid ${hasStock ? '#a7f3d0' : '#fecaca'}`,
            padding: '3px 8px',
            borderRadius: '12px',
            fontSize: '0.72rem',
            fontWeight: 800
          }}
        >
          {hasStock ? `${product.stock} un.` : 'Agotado'}
        </span>

        <img
          src={imageUrl}
          alt={product.nombre}
          onError={(e) => {
            (e.target as HTMLImageElement).src = `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${product.id}.png`;
          }}
          style={{
            width: '110px',
            height: '110px',
            objectFit: 'contain',
            filter: 'drop-shadow(0 6px 8px rgba(0, 0, 0, 0.12))'
          }}
        />
      </div>

     
      <div style={{ textAlign: 'center', width: '100%', display: 'flex', flexDirection: 'column', gap: '4px' }}>
        <h3
          style={{
            margin: 0,
            fontSize: '1.15rem',
            fontWeight: 800,
            color: '#0f172a',
            textTransform: 'capitalize',
            letterSpacing: '-0.2px'
          }}
        >
          {product.nombre}
        </h3>

        {product.tipo && (
          <div>
            <span
              style={{
                fontSize: '0.68rem',
                backgroundColor: '#f1f5f9',
                padding: '2px 8px',
                borderRadius: '10px',
                color: '#475569',
                fontWeight: 700,
                textTransform: 'uppercase',
                letterSpacing: '0.5px',
                border: '1px solid #e2e8f0'
              }}
            >
              {product.tipo}
            </span>
          </div>
        )}

        <div
          style={{
            marginTop: '4px',
            fontSize: '1.35rem',
            fontWeight: 800,
            color: '#1e1b4b'
          }}
        >
          ${product.precio.toLocaleString('es-CL')}
        </div>
      </div>

      
      <button
        onClick={() => onOrder(product)}
        disabled={!hasStock || isOrdering}
        style={{
          width: '100%',
          padding: '10px 14px',
          backgroundColor: hasStock ? (isOrdering ? '#cbd5e1' : '#f59e0b') : '#f1f5f9',
          color: hasStock ? (isOrdering ? '#475569' : '#0f172a') : '#94a3b8',
          border: 'none',
          borderRadius: '10px',
          fontWeight: 800,
          fontSize: '0.88rem',
          cursor: hasStock && !isOrdering ? 'pointer' : 'not-allowed',
          boxShadow: hasStock && !isOrdering ? '0 4px 12px rgba(245, 158, 11, 0.28)' : 'none',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '6px'
        }}
      >
        {isOrdering ? (
          <span>Procesando...</span>
        ) : hasStock ? (
          <>
            <span></span> Pedir Carta
          </>
        ) : (
          'Sin Stock'
        )}
      </button>
    </div>
  );
};