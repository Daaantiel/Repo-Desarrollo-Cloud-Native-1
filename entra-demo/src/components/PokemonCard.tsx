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
        border: '2px solid #2a75bb',
        borderRadius: '12px',
        padding: '16px',
        width: '220px',
        backgroundColor: '#ffffff',
        boxShadow: '0 4px 12px rgba(0, 0, 0, 0.1)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'space-between'
      }}
    >
  
      <div
        style={{
          backgroundColor: '#f5f5f5',
          borderRadius: '8px',
          padding: '10px',
          width: '100%',
          textAlign: 'center',
          marginBottom: '12px'
        }}
      >
        <img
          src={imageUrl}
          alt={product.nombre}
          onError={(e) => {
            // Reintenta con el sprite estándar si falla la imagen principal
            (e.target as HTMLImageElement).src = `https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${product.id}.png`;
          }}
          style={{
            width: '120px',
            height: '120px',
            objectFit: 'contain'
          }}
        />
      </div>


      <div style={{ textAlign: 'center', width: '100%' }}>
        <h3
          style={{
            margin: '0 0 8px 0',
            fontSize: '1.2rem',
            color: '#333',
            textTransform: 'capitalize'
          }}
        >
          {product.nombre}
        </h3>

        {product.tipo && (
          <span
            style={{
              fontSize: '0.75rem',
              backgroundColor: '#e0e0e0',
              padding: '3px 8px',
              borderRadius: '10px',
              color: '#555',
              fontWeight: 'bold',
              textTransform: 'uppercase'
            }}
          >
            {product.tipo}
          </span>
        )}

        <div
          style={{
            margin: '12px 0 6px 0',
            fontSize: '1.25rem',
            fontWeight: 'bold',
            color: '#2a75bb'
          }}
        >
          ${product.precio.toLocaleString('es-CL')}
        </div>

        <p
          style={{
            margin: '0 0 12px 0',
            fontSize: '0.85rem',
            fontWeight: '600',
            color: hasStock ? '#2e7d32' : '#c62828'
          }}
        >
          {hasStock ? `Stock: ${product.stock} un.` : '❌ Agotado'}
        </p>
      </div>

      {/* Botón para Comprar / Pedir */}
      <button
        onClick={() => onOrder(product)}
        disabled={!hasStock || isOrdering}
        style={{
          width: '100%',
          padding: '10px',
          backgroundColor: hasStock ? '#ffcb05' : '#e0e0e0',
          color: hasStock ? '#2a75bb' : '#9e9e9e',
          border: 'none',
          borderRadius: '6px',
          fontWeight: 'bold',
          fontSize: '0.9rem',
          cursor: hasStock && !isOrdering ? 'pointer' : 'not-allowed',
          boxShadow: hasStock ? '0 2px 4px rgba(0,0,0,0.15)' : 'none'
        }}
      >
        {isOrdering ? 'Procesando...' : hasStock ? '⚡ Pedir Carta' : 'Sin Stock'}
      </button>
    </div>
  );
};