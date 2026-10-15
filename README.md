# Minimarket SmartMarket - Sistema de Gestión de Ventas e Inventario

Este es el repositorio del sistema de escritorio para la gestión de ventas e inventario del minimarket "SmartMarket", desarrollado como parte del curso de Experiencias Formativas en Situaciones Reales de Trabajo II en Cibertec.

## 🛠️ Requisitos del Entorno

* **Java:** JDK 21
* **IDE:** Eclipse
* **Gestor de dependencias:** Maven
* **Base de Datos:** MySQL y  MySQL Workbench

## 🗄️ Configuración de la Base de Datos (MySQL)

1. Abrir **MySQL Workbench** y conectarse al servidor local.
2. Localizar el script de la base de datos (/sql/schema.sql).
3. Abrir el script en MySQL Workbench y ejecutarlo.

## 🚀 Importación y Ejecución en Eclipse

1. Clonar o descargar el proyecto.
2. Abrir **Eclipse**.
3. Usar la opción de importar desde proyectos Maven existentes y ubicar la carpeta raíz del proyecto.
6. Esperar a que Maven descargue las dependencias necesarias.
7. Buscar el archivo /src/main/resources/db.properties.example y seguir los pasos de conexión con la base de datos.
8. Ejecutar el proyecto desde /src/main/java/com/smartmarket/Main.java. La primera vez pedirá crear un usuario y contraseña para el primer administrador.
