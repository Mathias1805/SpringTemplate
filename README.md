# Servicio de Gestión de Sesiones con Doble Autenticación

## Descripción General

Sistema de autenticación y autorización desarrollado con Spring Boot que implementa un mecanismo de doble sesión mediante JWT. La arquitectura separa la validación de acceso a la API de la autenticación del cliente final, proporcionando dos capas de seguridad independientes con tokens especializados.

## Tecnologías Utilizadas

- **Spring Boot**: Framework principal para el desarrollo de la aplicación
- **Spring Security**: Gestión de autenticación y autorización
- **JWT (JSON Web Tokens)**: Tokens stateless para ambas capas de sesión
- **Spring Data JPA**: Persistencia de datos
- **PostgreSQL**: Base de datos relacional
- **BCrypt**: Encriptación de contraseñas

## Arquitectura de Doble Sesión

### Primera Capa: Validación de API

La primera capa de seguridad valida el acceso a la API mediante un token de aplicación. Este token identifica y autoriza a la aplicación cliente que está consumiendo los servicios, independientemente del usuario final.

#### Token de API

El token de API se genera al registrar una aplicación cliente y contiene información sobre los permisos y límites de uso de esa aplicación específica. Este token debe incluirse en todas las peticiones como requisito base para acceder a cualquier endpoint del sistema.

#### Propósito

Esta capa protege la API de accesos no autorizados a nivel de aplicación, permitiendo controlar qué sistemas externos pueden consumir los servicios, aplicar rate limiting por aplicación, y mantener métricas de uso diferenciadas.

### Segunda Capa: Autenticación de Cliente

Una vez validada la aplicación, la segunda capa autentica al usuario final que está utilizando esa aplicación. Esta capa genera un token específico basado en el tipo de cliente y sus permisos individuales.

#### Token de Cliente

El token de cliente se genera tras validar las credenciales del usuario y contiene información sobre su identidad, roles, permisos específicos, y tipo de cliente. Este token determina qué recursos y operaciones puede realizar el usuario dentro de la aplicación.

#### Tipos de Cliente

El sistema diferencia entre distintos tipos de cliente, cada uno con permisos y capacidades específicas. El token generado varía según el tipo de cliente autenticado, permitiendo una granularidad precisa en el control de acceso.

## Flujo de Autenticación Dual

### Fase 1: Validación de Aplicación

Toda petición al sistema debe incluir primero el token de API en el header correspondiente. El filtro de seguridad valida este token antes de procesar cualquier solicitud. Si el token de API es inválido, expirado, o la aplicación no tiene permisos para acceder al endpoint solicitado, la petición se rechaza inmediatamente.

### Fase 2: Autenticación de Usuario

Una vez validada la aplicación, se procede a autenticar al usuario. El cliente envía las credenciales del usuario junto con el token de API válido. El sistema valida las credenciales, determina el tipo de cliente, y genera un token personalizado con los permisos correspondientes.

### Fase 3: Acceso a Recursos

Para acceder a recursos protegidos, el cliente debe enviar ambos tokens: el token de API que identifica la aplicación, y el token de cliente que identifica y autoriza al usuario. Ambos tokens se validan en secuencia antes de permitir el acceso al recurso solicitado.

## Componentes del Sistema

### Security Configuration

Configuración central de Spring Security que define la cadena de filtros para la doble validación. Establece qué endpoints requieren solo validación de API, cuáles requieren ambas validaciones, y configura las políticas de sesión stateless para ambas capas.

### API Token Filter

Filtro que intercepta todas las peticiones para validar el token de API. Extrae el token del header, verifica su autenticidad y vigencia, carga los permisos de la aplicación, y valida que tenga autorización para el endpoint solicitado.

### Client Token Filter

Filtro que se ejecuta después de la validación del token de API. Extrae el token de cliente, valida su autenticidad, verifica que corresponda a un usuario activo, carga sus permisos específicos, y establece el contexto de seguridad del usuario.

### JWT Service

Servicio especializado en la gestión de tokens JWT para ambas capas. Genera tokens de API con claims relacionados con la aplicación cliente, y tokens de usuario con claims personalizados según el tipo de cliente y sus permisos. Maneja diferentes claves de firma y tiempos de expiración para cada tipo de token.

### Authentication Service

Servicio que orquesta el proceso completo de autenticación en ambas capas. Valida credenciales de aplicaciones y usuarios, determina tipos de cliente, asigna permisos, y coordina la generación de ambos tipos de tokens.

## Modelo de Datos

### Application

Entidad que representa una aplicación cliente autorizada para consumir la API. Almacena identificador único, nombre de la aplicación, clave secreta para generación de tokens, estado activo o inactivo, permisos asignados, límites de uso, y fechas de creación y expiración.

### User

Entidad principal de usuario del sistema. Contiene identificador único, nombre de usuario, email, contraseña encriptada, tipo de cliente, estado de la cuenta, fechas de registro y última actualización, y relación con roles y permisos.

### ClientType

Entidad que define los diferentes tipos de cliente soportados por el sistema. Cada tipo tiene un nombre identificador, descripción, conjunto de permisos base, límites específicos, y configuraciones particulares.

### Role

Entidad que representa roles asignables a usuarios. Almacena nombre del rol, descripción, y permisos asociados. Los roles se asignan según el tipo de cliente y pueden combinarse para crear perfiles de acceso complejos.

### Permission

Entidad que define permisos granulares del sistema. Cada permiso tiene un nombre único, descripción, recurso al que aplica, y tipo de operación permitida. Los permisos se agrupan en roles y se asignan dinámicamente según el tipo de cliente.

### ApiToken

Entidad que registra los tokens de API generados. Almacena el token hasheado, aplicación asociada, fecha de generación y expiración, estado de revocación, y metadatos de uso.

### ClientToken

Entidad que registra los tokens de cliente activos. Contiene el token hasheado, usuario asociado, tipo de cliente, fecha de generación y expiración, estado, y información de la sesión.

## Seguridad Implementada

### Encriptación

Todas las contraseñas se almacenan utilizando BCrypt con factor de trabajo elevado. Las claves secretas de aplicaciones se cifran antes de almacenarse. Los tokens sensibles se hashean antes de guardarse en base de datos.

### Separación de Concerns

La arquitectura de doble token separa completamente la autenticación de la aplicación de la autenticación del usuario. Esto permite revocar acceso a nivel de aplicación sin afectar a los usuarios, o viceversa. También facilita la implementación de políticas de seguridad diferenciadas para cada capa.

### Gestión de Tokens

Los tokens de API tienen tiempos de expiración largos y se renuevan mediante un proceso de refresh específico. Los tokens de cliente tienen vida más corta y se renuevan frecuentemente. Ambos tipos de tokens pueden ser revocados independientemente mediante blacklists mantenidas en base de datos.

### Validación Escalonada

Cada capa de validación se ejecuta secuencialmente. Si falla la validación del token de API, no se procede a validar el token de cliente. Esto optimiza el rendimiento y añade una capa adicional de protección contra ataques de fuerza bruta.

## Control de Acceso

### Permisos por Tipo de Cliente

Cada tipo de cliente tiene un conjunto base de permisos que determina su nivel de acceso al sistema. Los permisos se evalúan dinámicamente al procesar cada petición, permitiendo cambios de permisos sin requerir regeneración de tokens.

### Jerarquía de Roles

El sistema implementa una jerarquía de roles que se asignan según el tipo de cliente. Los roles heredan permisos de niveles inferiores y añaden capacidades adicionales. La evaluación de permisos considera tanto los roles heredados como los permisos directos.

### Contexto de Seguridad

El contexto de seguridad se construye combinando información de ambos tokens. Incluye datos de la aplicación, usuario, tipo de cliente, roles, permisos efectivos, y restricciones aplicables. Este contexto se utiliza para todas las decisiones de autorización.

## Configuración del Sistema

### Base de Datos PostgreSQL

El sistema utiliza PostgreSQL como base de datos principal. La configuración incluye pool de conexiones optimizado, índices en campos de búsqueda frecuente, y particionado de tablas de logs y tokens para mejor rendimiento.

### Claves y Secretos

Las claves de firma JWT se almacenan de forma segura en variables de entorno. Se utilizan claves diferentes para tokens de API y tokens de cliente. Las claves rotan periódicamente según políticas de seguridad definidas.

### Tiempos de Expiración

Los tiempos de expiración son configurables por tipo de token y tipo de cliente. Los tokens de API tienen expiración extendida. Los tokens de cliente varían según el nivel de seguridad requerido por el tipo de cliente.
