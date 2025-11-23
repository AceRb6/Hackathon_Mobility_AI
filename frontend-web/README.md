# 🌐 Frontend Web - Metro CDMX

Interfaz web para crear reportes de incidentes en el Metro de la Ciudad de México.

## 📁 Estructura de Archivos

```
frontend-web/
├── index.html          # Estructura HTML principal
├── css/
│   └── styles.css      # Estilos con tema Rojo/Blanco
└── js/
    └── app.js          # Lógica de la aplicación
```

## 🎨 Características

✅ **Diseño Metro CDMX** - Paleta de colores Rojo (#E3001B) y Blanco oficial
✅ **Autocompletado** - Búsqueda inteligente de estaciones mientras escribes
✅ **Validación en Tiempo Real** - Contador de caracteres y validación de campos
✅ **Responsive** - Se adapta a móviles y tablets
✅ **Datos de Prueba** - 20 estaciones de la Línea 1 pre-cargadas
✅ **Comentarios en Español** - Código completamente documentado

## 🚀 Cómo Ejecutar

### Opción 1: Abrir Directamente (Sin Servidor)
```bash
# Simplemente abre el archivo en tu navegador
cd frontend-web
start index.html  # En Windows
# o doble click en index.html
```

### Opción 2: Con Servidor Local (Recomendado)

**Usando Python:**
```bash
cd frontend-web
python -m http.server 8080
# Abre http://localhost:8080 en tu navegador
```

**Usando Node.js (npx):**
```bash
cd frontend-web
npx serve
```

**Usando VS Code:**
- Instala la extensión "Live Server"
- Click derecho en `index.html` → "Open with Live Server"

## 🔌 Conectar con el Backend

Para conectar con tu backend FastAPI, edita el archivo `js/app.js`:

```javascript
// Busca la función enviarReporte() y descomenta:
async function enviarReporte() {
    const reporte = { ... };
    
    // Descomenta estas líneas:
    const response = await fetch('http://localhost:8000/api/reportes', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(reporte)
    });
}
```

No olvides habilitar CORS en tu backend FastAPI:
```python
from fastapi.middleware.cors import CORSMiddleware

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)
```

## 📱 Funcionalidades Implementadas

1. **Búsqueda de Estaciones**
   - Autocompletado con filtrado en tiempo real
   - Máximo 5 sugerencias
   - Validación de estación cerrada

2. **Campo de Descripción**
   - Límite de 350 caracteres
   - Contador visual
   - Validación automática

3. **Envío de Reporte**
   - Botón deshabilitado hasta completar campos
   - Modal de confirmación
   - Limpieza automática del formulario

4. **Modales Informativos**
   - Alerta de estación cerrada
   - Confirmación de envío exitoso

## 🎯 Próximos Pasos

- [ ] Conectar con el backend FastAPI
- [ ] Agregar más estaciones (otras líneas)
- [ ] Implementar historial de reportes
- [ ] Añadir autenticación de usuario

---

**Desarrollado con ❤️ para el Metro CDMX**
