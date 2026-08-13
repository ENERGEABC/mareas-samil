# Mareas Samil - Widget de mareas para Vigo

Proyecto Android Studio completo con un widget de escritorio que dibuja un reloj de mareas (aguja + arco azul/rojo) para la Playa de Samil (ria de Vigo).

## Fuente de datos

Servicio publico y gratuito de MeteoGalicia (Xunta de Galicia), puerto de Vigo (idPorto=3):

https://servizos.meteogalicia.gal/mgrss/predicion/mareas/jsonMareas.action?idPorto=3

Samil esta dentro de la ria de Vigo, asi que las horas de pleamar y bajamar del puerto de Vigo son la referencia estandar tambien para la playa.

La API solo permite pedir 30 dias por consulta, asi que la app descarga los proximos 30 dias al instalarse y vuelve a sincronizar una vez al dia en segundo plano con WorkManager. Cada 15 minutos recalcula la posicion de la aguja a partir de la cache local, sin necesitar red.

## Estructura del proyecto

TideRepository.kt descarga, cachea y calcula el estado de la marea.

TideDialRenderer.kt dibuja el reloj en un Bitmap.

TideWidgetProvider.kt es el AppWidgetProvider del widget.

TideWorkers.kt hace la sincronizacion diaria y el redibujado cada 15 minutos.

MainActivity.kt es la pantalla minima que dispara la primera sincronizacion.

## Compilar el APK automaticamente con GitHub Actions

Este repositorio incluye .github/workflows/build.yml, que compila el APK en los servidores de GitHub cada vez que se sube codigo a main.

Pasos: entra en la pestana Actions de este repositorio, espera a que termine el workflow Build APK (unos 3-5 minutos), y descarga el artefacto mareas-samil-apk. Descomprimelo para obtener app-debug.apk e instalalo en tu movil.

## Compilar con Android Studio

Abre la carpeta del proyecto en Android Studio, deja que Gradle sincronice, y pulsa Run en un movil o emulador con Android 8.0 (API 26) o superior. Luego manten pulsado en la pantalla de inicio, entra en Widgets, busca Mareas Samil y arrastralo a la pantalla.
