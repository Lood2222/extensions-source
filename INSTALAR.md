# Rncalation con proxy — cómo instalarlo y usarlo

Esta es la extensión **Rncalation** (la de Keiyoushi) **parcheada**: además de lo de siempre,
sabe leer los capítulos a través del **proxy de desbloqueo** (`rncalation-proxy`). Sin el proxy
funciona igual que la oficial, pero los capítulos bloqueados no se pueden abrir.

Datos técnicos, por si hacen falta:

| Cosa | Valor |
|---|---|
| Nombre en la app | `Rncalation` |
| `applicationId` | `eu.kanade.tachiyomi.extension.es.nartag` (**el mismo que la oficial**) |
| Versión | `versionCode` 65 · `versionName` 1.4.65 |
| Firma | propia (`CN=Rncalation Proxy Build`), **no** la de Keiyoushi |
| APK | [`rncalation-proxy-v1.4.65.apk`](https://github.com/Lood2222/extensions-source/releases/download/rncalation-proxy-v1/rncalation-proxy-v1.4.65.apk) |
| Repo de extensiones | `https://raw.githubusercontent.com/Lood2222/extensions-source/feat/T3/index.min.json` |

> ⚠️ **Lo más importante de todo:** como se llama igual que la extensión oficial pero está
> firmada con otra clave, **Android no deja actualizar una encima de la otra**. Antes de instalar
> esta, **desinstala la extensión "Rncalation" oficial** (Ajustes → Extensiones → Rncalation →
> Desinstalar). Si no, verás un error tipo *"Aplicación no instalada"* o *"App not installed"*.

---

## Opción A (recomendada): añadir el repositorio de extensiones

Así la app te avisa de actualizaciones y no tienes que buscar el APK.

### En Mihon (o Tachiyomi y derivados)

1. Abre **Más → Ajustes → Explorar** (en algunas versiones: *Ajustes → Avanzado → Explorar*).
2. Entra en **Repositorios de extensiones**.
3. Pulsa **+ / Añadir** y pega exactamente esta URL:

   ```
   https://raw.githubusercontent.com/Lood2222/extensions-source/feat/T3/index.min.json
   ```

4. Acepta. Vuelve a **Explorar → Extensiones**, baja a la pestaña **ES (español)** y busca
   **Rncalation**: aparecerá con la etiqueta del repo `RNC`. Pulsa **Instalar**.
5. Cuando la app pregunte, **confía** en la extensión (es tuya).

### En Suwayomi

1. Abre la web de Suwayomi → **Ajustes → Extensiones**.
2. En *Repositorios de extensiones* pulsa **Añadir** y pega la misma URL:

   ```
   https://raw.githubusercontent.com/Lood2222/extensions-source/feat/T3/index.min.json
   ```

3. Pulsa el botón de **buscar extensiones nuevas** y luego instala **Rncalation**.

> Si tu versión de la app es antigua y el repositorio no carga o la descarga falla, usa la
> **Opción B**: el APK a mano.

---

## Opción B: instalar el APK a mano

1. **Desinstala antes la extensión oficial** "Rncalation" (misma clave distinta = no se puede
   actualizar encima). Esto borra el ajuste del proxy, tendrás que volver a ponerlo (paso 4).
2. En el móvil, abre este enlace y descarga el APK:

   https://github.com/Lood2222/extensions-source/releases/download/rncalation-proxy-v1/rncalation-proxy-v1.4.65.apk

3. Al abrirlo, Android dirá que **no se pueden instalar apps de origen desconocido**. Pulsa
   **Ajustes** en ese aviso y activa el permiso para el navegador (o para el gestor de archivos)
   que estés usando. Vuelve atrás y pulsa **Instalar**.
4. Abre Mihon → **Explorar → Extensiones** y comprueba que sale **Rncalation** instalada. Si no
   aparece, cierra y abre la app (o *Ajustes → Avanzado → Recargar extensiones*).

---

## Configurar el proxy (solo hace falta para capítulos bloqueados)

La extensión no necesita el proxy para navegar, buscar ni leer los capítulos normales. Lo
necesita para los **últimos capítulos**, que la web esconde detrás de un anuncio.

1. Arranca el proxy en el ordenador donde lo tengas: doble clic en **`iniciar.bat`** (Windows) o
   `./iniciar.sh` (Linux/macOS). Escucha en el puerto **9193**. Comprueba que responde abriendo
   <http://localhost:9193/health> en el navegador de ese ordenador.
2. En Mihon: **Explorar → Extensiones → Rncalation → ⚙ (Ajustes de la extensión)**. También se
   llega desde **Explorar → Fuentes → Rncalation → Ajustes**.
3. Rellena **Proxy URL** con la dirección del proxy **sin barra final**:

   | Dónde corre el proxy | Qué poner |
   |---|---|
   | En el mismo móvil que Mihon | `http://127.0.0.1:9193` |
   | En tu PC, y el móvil está en la misma wifi | `http://IP-DEL-PC:9193` |
   | En un servidor de casa (siempre encendido) | `http://IP-DEL-SERVIDOR:9193` |

   Para saber la IP del PC: en Windows, abre *CMD* y escribe `ipconfig`; la línea **Dirección
   IPv4** (algo como `192.168.1.50`) es la que va en la URL. **Ojo:** el móvil y el PC tienen que
   estar en la misma red, y el **firewall de Windows** tiene que dejar pasar el puerto 9193
   (la primera vez Windows pregunta; di *Permitir* en redes privadas).
4. Deja el campo **vacío** si no quieres usar el proxy: entonces todo funciona como la extensión
   oficial y los capítulos bloqueados dan error.

---

## Qué pasa si el capítulo está bloqueado

Al abrir un capítulo bloqueado verás un mensaje en español, del estilo:

> **Capítulo bloqueado. La web no muestra las páginas hasta que lo desbloqueas una vez.**
> Desbloquéalo con desbloquear.bat (o ejecutando `node tools/unlock.mjs --hash …`) y vuelve a
> intentarlo.
> Detalle: `LOCKED_MANUAL` … Enlace de desbloqueo: `https://…`

Eso **no es un fallo de la extensión**: la web exige que una **persona** pase un anuncio y un
captcha (Cloudflare Turnstile) una vez por capítulo. El proxy no puede hacerlo solo. Qué hacer:

1. En el PC donde está el proxy, doble clic en **`desbloquear.bat`** (o `./desbloquear.sh`).
   Se abre un Chromium de verdad, te pide el número de capítulo, pulsas el botón de desbloquear
   de la web, pasas el anuncio y resuelves el captcha. Tarda 1–3 minutos.
2. El script guarda la sesión y la importa en el proxy automáticamente.
3. Vuelve a Mihon y **reabre el capítulo**. Ya se lee (y queda en caché: los siguientes intentos
   van directos).

Otros mensajes que puedes ver y qué significan:

| Mensaje | Qué pasa |
|---|---|
| *El proxy no responde en …* | El proxy está apagado o la IP/puerto están mal. Arranca `iniciar.bat` y revisa la URL. |
| *La URL del proxy no es válida…* | Falta `http://` delante. |
| *El proxy (…) respondió 409…* | Capítulo bloqueado: hay que desbloquearlo (arriba). |
| *El proxy (…) no devolvió ninguna página…* | El desbloqueo caducó: vuelve a pasar `desbloquear.bat`. |

---

## Cómo volver a la extensión oficial

1. **Ajustes → Extensiones → Rncalation → Desinstalar** (o desinstálala desde los ajustes de
   Android).
2. Si añadiste este repositorio, quítalo: **Ajustes → Explorar → Repositorios de extensiones →
   eliminar `RNC` / `Rncalation Proxy`** (si lo dejas, te seguirá ofreciendo la nuestra).
3. Instala la oficial: añade el repo de Keiyoushi
   (`https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json`) si no lo tienes,
   y en **Explorar → Extensiones → ES** instala **Rncalation**.
4. Vuelve a iniciar sesión en la fuente si te lo pide. Tus mangas, biblioteca e historial **no se
   pierden**: van en la app, no en la extensión.

> Nota: la oficial y esta comparten `applicationId`, así que **no pueden estar instaladas a la
> vez**. Y no intentes "actualizar" de una a otra: al tener firmas distintas, Android lo rechaza.
> Siempre desinstalar → instalar.
