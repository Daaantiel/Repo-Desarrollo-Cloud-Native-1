import React from 'react';

export type OrderStatus = 'COMPLETADO' | 'CREADO' | string;

interface OrderStatusBadgeProps {
  status: OrderStatus;
}


const STATUS_STYLES: Record<string, { bg: string; text: string; label: string }> = {
  COMPLETADO: { bg: '#e8f5e9', text: '#2e7d32', label: 'Completado' },
  CREADO:     { bg: '#e3f2fd', text: '#1565c0', label: 'Creado' }
};

export const OrderStatusBadge: React.FC<OrderStatusBadgeProps> = ({ status }) => {
  const key = (status || '').toUpperCase();


  const config = STATUS_STYLES[key] || {
    bg: '#f5f5f5',
    text: '#616161',
    label: status
  };

  return (
    <span
      style={{
        backgroundColor: config.bg,
        color: config.text,
        padding: '4px 12px',
        borderRadius: '16px',
        fontSize: '0.85rem',
        fontWeight: 'bold',
        display: 'inline-flex',
        alignItems: 'center',
        border: `1px solid ${config.text}40`
      }}
    >
      {config.label}
    </span>
  );
};