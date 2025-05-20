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
