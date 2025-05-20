## Huellas SringBootAPI

### Endpoint de registro

- Endpoint

```bash
POST url/api/huellas/registrar
```

- Body (FormData)

```bash
userId -> nombre de usuario
fingerprint[] -> base64 de la huella, enviar minimo 2
```

### Endpoint de validacion

- Endpoint

```bash
POST url/api/huellas/validar
```

- Body (FormData)

```bash
userId -> nombre de usuario
fingerprint -> base64 de la captura
```

### Formato de respuesta

- Status codes 200/400/500
- Payloads

```json
{
  "ok": "boolean",
  "message": "string"
}
```

### Iniciar proyecto

- Crear archivo `.env` con el secreto que comparte el sistema con la api
- Utilizar docker para la build o el docker compose con los puertos que se quieran exponer
- Si no se usa Docker asegurarse de tener instalado Maven y Java 17+
