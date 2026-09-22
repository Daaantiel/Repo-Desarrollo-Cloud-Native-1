import React from 'react';

export type OrderStatus = 'COMPLETADO' | 'CREADO' | string;

interface OrderStatusBadgeProps {
  status: OrderStatus;
}

const STATUS_STYLES: Record<string, { bg: string; text: string; border: string; dot: string; label: string }> = {
  COMPLETADO: { 
    bg: '#ecfdf5', 
    text: '#047857', 
    border: '#a7f3d0', 
    dot: '#10b981', 
    label: 'Completado' 
  },
  CREADO: { 
    bg: '#eff6ff', 
    text: '#1d4ed8', 
    border: '#bfdbfe', 
    dot: '#442ff9', 
    label: 'Creado' 
  }
};

export const OrderStatusBadge: React.FC<OrderStatusBadgeProps> = ({ status }) => {
  const key = (status || '').toUpperCase();

  const config = STATUS_STYLES[key] || {
    bg: '#f8fafc',
    text: '#475569',
    border: '#e2e8f0',
    dot: '#94a3b8',
    label: status
  };

  return (
    <span
      style={{
        backgroundColor: config.bg,
        color: config.text,
        border: `1px solid ${config.border}`,
        padding: '4px 12px',
        borderRadius: '20px',
        fontSize: '0.8rem',
        fontWeight: 700,
        display: 'inline-flex',
        alignItems: 'center',
        gap: '6px',
        boxShadow: '0 1px 2px rgba(0, 0, 0, 0.03)',
        letterSpacing: '0.2px',
        width: 'fit-content'
      }}
    >
     
      <span
        style={{
          width: '6px',
          height: '6px',
          borderRadius: '50%',
          backgroundColor: config.dot,
          boxShadow: `0 0 6px ${config.dot}`
        }}
      />
      {config.label}
    </span>
  );
};