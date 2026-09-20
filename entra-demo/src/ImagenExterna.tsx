function ImagenExterna() {
  return (
    <div>
      <h1>Imagen desde Internet</h1>
      {/* Recuerda usar un enlace directo a la imagen (.jpg, .png o URL de imagen de Unsplash) */}
      <img 
        src="https://www.google.com/search?q=meowl&oq=meowl&gs_lcrp=EgZjaHJvbWUyBggAEEUYOTIMCAEQABhDGIAEGIoFMgwIAhAAGEMYgAQYigUyDAgDEAAYQxiABBiKBTIHCAQQABiABDIHCAUQABiABDIMCAYQABhDGIAEGIoFMgcIBxAAGIAEMgcICBAAGIAEMgcICRAAGIAE0gEIMjUxMWowajeoAgCwAgA&sourceid=chrome&source=chrome.ob&ie=UTF-8#sv=CAMSZxowKg5kWUw2MFJnSXItMDV0TTIOZFlMNjBSZ0lyLTA1dE06DjAtMVNtNlZiRm53elVNIAQqLwobXy13dWphdnZyRmZIYjFzUVA1dHpVOFF3XzcwEg5kWUw2MFJnSXItMDV0TRgAMAEYByCBicPeDUoIEAEYASABKAE" 
        alt="meowl" 
        style={{ width: '100%', maxWidth: '500px', borderRadius: '8px' }}
      />
    </div>
  );
}

export default ImagenExterna;