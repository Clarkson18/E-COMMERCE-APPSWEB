# E-COMMERCE-APPSWEB

Aplicación de comercio electrónico desarrollada con Java 21, Spring Boot,
Thymeleaf, Spring Data JPA y MySQL.

## Requisitos

- JDK 21.
- MySQL instalado y en ejecución, con un usuario que pueda crear y modificar tablas.
- IntelliJ IDEA u otro IDE compatible con Java y Maven.
- Conexión a internet para descargar dependencias en la primera ejecución.

El proyecto incluye Maven Wrapper; no es necesario instalar Maven por separado.
MySQL Workbench es opcional y permite consultar la base de datos.

## 1. Preparar la base de datos

La configuración predeterminada utiliza:

| Propiedad | Valor |
| --- | --- |
| Servidor | `localhost` |
| Puerto | `3306` |
| Base de datos | `ecommerce` |
| Usuario | `root` |

La aplicación intentará crear `ecommerce` si no existe. Si el usuario no tiene
permiso para crear bases de datos, solicita su creación o ejecuta con una cuenta
autorizada en MySQL Workbench:

```sql
CREATE DATABASE IF NOT EXISTS ecommerce;
```

## 2. Configurar las credenciales locales

Crea el archivo `application-local.properties` dentro de la carpeta `E-Commerce/`,
al mismo nivel que `pom.xml`:

```text
E-Commerce/
├── pom.xml
├── application-local.properties
└── src/
```

Agrega tus credenciales de MySQL y sustituye `TU_CONTRASENA` por tu contraseña:

```properties
spring.datasource.username=root
spring.datasource.password=TU_CONTRASENA
```

Cada integrante debe crear este archivo en su equipo. Está excluido de Git.
En Windows, verifica que no se guarde como `application-local.properties.txt`.

Si utilizas otro servidor o puerto, agrega también la URL correspondiente:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce?serverTimezone=UTC&createDatabaseIfNotExist=true
```

## 3. Ejecutar desde IntelliJ IDEA

1. Abre `E-Commerce/pom.xml` como proyecto Maven. Si ya tienes abierto el
   repositorio, haz clic derecho en ese archivo y selecciona **Add as Maven Project**.
2. Espera a que Maven termine de descargar las dependencias.
3. En **File → Project Structure**, selecciona **JDK 21** como Project SDK.
4. Abre `src/main/java/com/example/E_Commerce/ECommerceApplication.java`.
5. Ejecuta el método `main` con el botón verde junto a la clase o al método.

En la configuración de ejecución, utiliza JDK 21 y la carpeta `E-Commerce/`
como **Working directory**. También se admite la raíz del repositorio para cargar
el archivo local de credenciales.

Cuando la consola muestre `Started ECommerceApplication`, abre:

**http://localhost:8080**

Para detener la aplicación, utiliza el botón **Stop** del IDE.

## Ejecutar desde la terminal

Abre una terminal en `E-Commerce/` y comprueba que `java -version` indique Java 21.

**Windows (PowerShell):**

```powershell
.\mvnw.cmd spring-boot:run
```

**Linux o macOS:**

```bash
bash mvnw spring-boot:run
```

Detén la aplicación con `Ctrl+C`.

## Verificar las tablas

JPA crea o actualiza las tablas al iniciar mediante `spring.jpa.hibernate.ddl-auto=update`.
Esta configuración se utiliza para desarrollo.

En MySQL Workbench, actualiza **Schemas → ecommerce → Tables**, o ejecuta:

```sql
USE ecommerce;
SHOW TABLES;
```

Deben aparecer `usuarios`, `productos`, `pedidos`, `detalles_pedido` y `resenias`.
La creación de tablas no agrega automáticamente usuarios ni productos.

## Ejecutar las pruebas

Desde `E-Commerce/`:

```powershell
# Windows
.\mvnw.cmd test
```

```bash
# Linux o macOS
bash mvnw test
```

Las pruebas usan H2 en memoria y no modifican la base MySQL local.

## Problemas frecuentes

- **No aparece el botón para ejecutar:** importa `E-Commerce/pom.xml` como
  proyecto Maven y espera a que termine la sincronización.
- **Access denied for user:** verifica el usuario y la contraseña en
  `application-local.properties`, su ubicación y la carpeta de trabajo.
- **No se puede conectar a la base:** comprueba que el servicio MySQL esté
  encendido y que el servidor y puerto coincidan con la configuración.
- **Puerto 8080 ocupado:** detén la otra aplicación o agrega
  `server.port=8081` al archivo local y abre `http://localhost:8081`.
- **Errores después de cambiar el `pom.xml`:** recarga el proyecto desde el panel
  Maven de IntelliJ y verifica que el IDE y Maven utilicen JDK 21.
