# Rncalation con proxy — cómo instalarlo y usarlo

Esta es la extensión **Rncalation** (la de Keiyoushi) **parcheada**: además de lo de siempre,
sabe leer los capítulos a través del **proxy de desbloqueo**
([`Lood2222/rncalation-proxy`](https://github.com/Lood2222/rncalation-proxy), repositorio
**privado**). Sin el proxy funciona igual que la oficial, pero los capítulos bloqueados no se
pueden abrir.

Datos técnicos, por si hacen falta:

| Cosa | Valor |
|---|---|
| Nombre en la app | `Rncalation Proxy` |
| `applicationId` | `eu.kanade.tachiyomi.extension.es.rncalationproxy` (**propio, distinto del de la oficial**) |
| Versión | `versionCode` 67 · `versionName` 1.4.67 |
| Firma | propia (`CN=Rncalation Proxy Build`), **no** la de Keiyoushi |
| APK | [`rncalation-proxy-v1.4.66.apk`](https://github.com/Lood2222/extensions-source/releases/download/rncalation-proxy-v2/rncalation-proxy-v1.4.66.apk) |
| Release | <https://github.com/Lood2222/extensions-source/releases/tag/rncalation-proxy-v2> |
| Repo de extensiones | `https://raw.githubusercontent.com/Lood2222/extensions-source/rncalation-proxy/index.min.json` |

> ✅ **Lo más importante de todo:** esta extensión tiene **paquete propio**
> (`…es.rncalationproxy`) y **nombre propio** en la app (**Rncalation Proxy**), así que
> **convive con la oficial** (Rncalation de Keiyoushi): puedes tener las dos instaladas a la vez,
> cada una con sus fuentes y sus ajustes. No hace falta desinstalar nada.
>
> Ojo con una cosa: al ser paquetes distintos, **son dos aplicaciones separadas**. Si venías de
> la versión anterior de este parche (la que compartía paquete con la oficial), esa sí hay que
> desinstalarla: `versionCode` 67 tiene otro `applicationId` y Android la trata como otra app.
> Tu biblioteca vive en Mihon, no en la extensión, pero las entradas que apuntaban a la fuente
> antigua hay que volver a enlazarlas desde la fuente **Rncalation Proxy**.

---

## Opción A (recomendada): añadir el repositorio de extensiones

Así la app te avisa de actualizaciones y no tienes que buscar el APK.

### En Mihon (o Tachiyomi y derivados)

1. Abre **Más → Ajustes → Explorar** (en algunas versiones: *Ajustes → Avanzado → Explorar*).
2. Entra en **Repositorios de extensiones**.
3. Pulsa **+ / Añadir** y pega exactamente esta URL:

   ```
   https://raw.githubusercontent.com/Lood2222/extensions-source/rncalation-proxy/index.min.json
   ```

4. Acepta. Vuelve a **Explorar → Extensiones**, baja a la pestaña **ES (español)** y busca
   **Rncalation Proxy**: aparecerá con la etiqueta del repo `RNC`. Pulsa **Instalar**.
5. Cuando la app pregunte, **confía** en la extensión (es tuya).

### En Suwayomi

1. Abre la web de Suwayomi → **Ajustes → Extensiones**.
2. En *Repositorios de extensiones* pulsa **Añadir** y pega la misma URL:

   ```
   https://raw.githubusercontent.com/Lood2222/extensions-source/rncalation-proxy/index.min.json
   ```

3. Pulsa el botón de **buscar extensiones nuevas** y luego instala **Rncalation Proxy**.

> Si tu versión de la app es antigua y el repositorio no carga o la descarga falla, usa la
> **Opción B**: el APK a mano.

---

## Opción B: instalar el APK a mano

1. No hace falta desinstalar nada: el APK se instala **al lado** de la extensión oficial.
2. En el móvil, abre este enlace y descarga el APK:

   https://github.com/Lood2222/extensions-source/releases/download/rncalation-proxy-v2/rncalation-proxy-v1.4.66.apk

3. Al abrirlo, Android dirá que **no se pueden instalar apps de origen desconocido**. Pulsa
   **Ajustes** en ese aviso y activa el permiso para el navegador (o para el gestor de archivos)
   que estés usando. Vuelve atrás y pulsa **Instalar**.
4. Abre Mihon → **Explorar → Extensiones** y comprueba que sale **Rncalation Proxy** instalada. Si no
   aparece, cierra y abre la app (o *Ajustes → Avanzado → Recargar extensiones*).

---

## Configurar el proxy (solo hace falta para capítulos bloqueados)

La extensión no necesita el proxy para navegar, buscar ni leer los capítulos normales. Lo
necesita para los **últimos capítulos**, que la web esconde detrás de un anuncio.

1. **Descarga el proxy.** Ojo: el repositorio del proxy es **privado**, así que un `git clone`
   normal falla (pide usuario y contraseña) y un enlace "en crudo" sin sesión da 404. Se hace
   desde la web, con tu sesión de GitHub abierta:

   1. Abre <https://github.com/Lood2222/rncalation-proxy> **con tu sesión de GitHub iniciada**.
      Si no has iniciado sesión verás un 404: es normal, el repo es privado.
   2. Pulsa el botón verde **`Code` → `Download ZIP`**.
   3. Descomprime el ZIP en una carpeta, por ejemplo `C:\rncalation-proxy`.
   4. Doble clic en **`iniciar.bat`** (Windows) o `./iniciar.sh` (Linux/macOS). Levanta el proxy y
      su FlareSolverr; escucha en el puerto **9193**.
   5. Comprueba que responde abriendo <http://localhost:9193/health> en el navegador de ese
      ordenador.

   > ℹ️ La rama por defecto del repo ya es **`rncalation-proxy`**, que es donde está el proxy:
   > el botón **`Code` → `Download ZIP`** descarga lo correcto y **no hay que cambiar de rama**.
   > Si prefieres un enlace directo (o quieres asegurarte), este ZIP es exactamente el mismo:
   >
   > <https://github.com/Lood2222/rncalation-proxy/archive/refs/heads/rncalation-proxy.zip>
   >
   > (Dentro del ZIP la carpeta se llama `rncalation-proxy-rncalation-proxy`; ábrela y ahí están
   > `iniciar.bat`, `desbloquear.bat` y `parar.bat`.)
   >
   > El repositorio es **privado**: si abres el enlace sin sesión de GitHub verás un 404.
   > Si algún día lo quieres tener en otro sitio o con `git`, necesitarás un **token de GitHub**
   > (`gh auth login`, o un PAT con permiso de lectura del repo), porque el repositorio es
   > privado.
   >
   > Dentro del ZIP hay un **`LEEME.md`** escrito para alguien que no sabe nada de informática:
   > instalación de Docker y Node, los 5 pasos y una tabla de problemas. Es la guía del proxy;
   > este documento solo cuenta lo de la extensión.
2. En Mihon: **Explorar → Extensiones → Rncalation Proxy → ⚙ (Ajustes de la extensión)**. También se
   llega desde **Explorar → Fuentes → Rncalation Proxy → Ajustes**.
3. Rellena **Proxy URL** con la dirección del proxy **sin barra final**:

   | Dónde corre el proxy | Qué poner |
   |---|---|
   | En el **mismo** móvil/PC que Mihon | `http://localhost:9193` |
   | En tu PC, y el móvil está en la misma wifi | `http://IP-DEL-PC:9193` |

   Para saber la IP del PC: en Windows, abre *CMD* y escribe `ipconfig`; la línea **Dirección
   IPv4** (algo como `192.168.1.50`) es la que va en la URL. **Ojo:** el móvil y el PC tienen que
   estar en la misma red, y el **firewall de Windows** tiene que dejar pasar el puerto 9193
   (la primera vez Windows pregunta; di *Permitir* en redes privadas).
4. Deja el campo **vacío** si no quieres usar el proxy: entonces todo funciona como la extensión
   oficial y los capítulos bloqueados dan error.

---

## Qué pasa si el capítulo está bloqueado

Al abrir un capítulo bloqueado verás un mensaje en español, del estilo:

> **Capítulo bloqueado: la web no muestra las páginas hasta que lo desbloqueas una vez.**
>
> 1. En el ordenador, ejecuta el comando del proxy:
>    `node tools/unlock.mjs --slug el-tutorial-es-demasiado-dificil --num 287 --out session.json`
>
> 2. Abre este enlace en el navegador y resuelve el captcha:
>    `https://cuty.io/…`
>
> Cuando termine, vuelve a intentarlo.

Eso **no es un fallo de la extensión**: la web exige que una **persona** pase un anuncio y un
captcha (Cloudflare Turnstile) una vez por capítulo. El proxy no puede hacerlo solo. Qué hacer:

1. En el PC donde está el proxy, doble clic en **`desbloquear.bat`** (Windows) o `./desbloquear.sh`
   (Linux/macOS), en la carpeta que descargaste en el paso 1. Te pide el **número de
   capítulo** (y el cómic si no es el de por defecto) y hace todo lo demás:
   - Abre un Chromium de verdad en el capítulo.
   - Tú pulsas el botón de desbloquear, pasas el anuncio y resuelves el captcha.
   - Exporta la sesión y **la importa en el proxy** automáticamente.

   Equivalente a mano, si prefieres la terminal (ya **no hace falta hash**, el proxy lo resuelve
   por cómic y número de capítulo):

   ```bash
   node tools/unlock.mjs --slug el-tutorial-es-demasiado-dificil --num 287 --out session.json
   ```

   Tarda 1–3 minutos.
2. Vuelve a Mihon y **reabre el capítulo**. Ya se lee (y queda en caché: los siguientes intentos
   van directos).

Otros mensajes que puedes ver y qué significan:

| Mensaje | Qué pasa |
|---|---|
| *El proxy no responde en …* | El proxy está apagado o la IP/puerto están mal. Arranca `iniciar.bat` y revisa la URL. |
| *La URL del proxy no es válida…* | Falta `http://` delante. |
| *El proxy (…) respondió 409…* | Capítulo bloqueado: hay que desbloquearlo (arriba). El propio mensaje trae el comando y el enlace. |
| *Es un fallo temporal del proxy…* | El proxy estaba ocupado o la web tardó demasiado: reinténtalo en un par de minutos. |
| *El proxy (…) no devolvió ninguna página…* | El desbloqueo caducó: vuelve a pasar `desbloquear.bat`. |

---

## Cómo volver a la extensión oficial

1. Si la oficial **no** está instalada (puede estarlo: no chocan), instálala: añade el repo de
   Keiyoushi (`https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json`) si no
   lo tienes, y en **Explorar → Extensiones → ES** instala **Rncalation**.
2. Si quieres quitar la parcheada: **Ajustes → Extensiones → Rncalation Proxy → Desinstalar** (o
   desinstálala desde los ajustes de Android).
3. Si añadiste este repositorio, quítalo: **Ajustes → Explorar → Repositorios de extensiones →
   eliminar `RNC` / `Rncalation Proxy`** (si lo dejas, te seguirá ofreciendo la nuestra).
4. Vuelve a iniciar sesión en la fuente si te lo pide. Tus mangas, biblioteca e historial **no se
   pierden**: van en la app, no en la extensión.

> Nota: la oficial y esta tienen **paquetes distintos**, así que **pueden estar instaladas a la
> vez**. Ojo: no intentes "actualizar" de una a otra, son aplicaciones independientes; se instala
> y se desinstala cada una por su lado.
