# slides-rmi

Presentación de diapositivas controlada por red con **Java RMI**.


mvn compile exec:java -Dexec.mainClass=classes.Main

El **servidor** abre el PDF y lo proyecta en su pantalla. El **cliente** es un mando a
distancia: solo envía órdenes. Por la red nunca viajan imágenes.

---
## Compilar

Cada proyecto se compila por separado, desde su propia carpeta.

```bash
cd slides-server && mvn clean package
cd ../slides-client && mvn clean package
```

Genera:

- `slides-server/target/slides-server.jar`
- `slides-client/target/slides-client.jar`

### Después de cambiar código

Vuelve a correr `mvn clean package` **solo en el proyecto que tocaste**.

> **Si cambiaste `iRMI.java`:** el archivo está duplicado en los dos proyectos y tiene
> que ser idéntico. Copia el cambio al otro proyecto y recompila **ambos**. Si las
> versiones no coinciden, el cliente falla al conectar con un error confuso.
>
> Verificar que coinciden:
> ```bash
> diff slides-server/src/main/java/classes/iRMI.java \
>      slides-client/src/main/java/classes/iRMI.java && echo IDENTICOS
> ```

---

## Ejecutar

### Servidor

Desde la carpeta donde esté el PDF:

```bash
cd slides-server
java -jar target/slides-server.jar
```

Con otro archivo o una carpeta de imágenes:

```bash
java -jar target/slides-server.jar otra-presentacion.pdf
java -jar target/slides-server.jar mis-imagenes/
```

Imprime la dirección donde quedó publicado:

```
Servidor listo en  rmi://192.168.1.50:1099/control
```

### Cliente

```bash
cd slides-client
java -jar target/slides-client.jar <ip-del-servidor> <nombre>
```

Ejemplos:

```bash
java -jar target/slides-client.jar localhost cliente-A
java -jar target/slides-client.jar 192.168.1.50 cliente-B
```

Sin argumentos usa `localhost` y `cliente-A`.

Al arrancar, el cliente **espera** a que el operador apruebe la conexión en la pantalla
del servidor. Si lo rechazan, el programa termina.

Puedes conectar varios clientes a la vez, cada uno con un nombre distinto.

---

## Cómo funciona

**Contrato.** `iRMI` es la única interfaz compartida. El servidor la implementa
(`ImpRMI extends UnicastRemoteObject`), el cliente solo la consume a través del stub
que obtiene con `Naming.lookup`.

**Permiso.** El cliente llama a `conectar(nombre)`. El servidor muestra un diálogo al
operador; si aprueba, devuelve un token (UUID). El cliente reenvía ese token en cada
llamada. Sin token válido, todo se rechaza con `-1`.

**Una acción por segundo.** El servidor descarta las órdenes remotas que lleguen menos
de 1 s después de la anterior, vengan del mando que vengan. Los métodos de `ImpRMI` son
`synchronized`, así que los hilos de RMI entran de uno en uno: el primero marca la hora
y ejecuta, los demás ven la marca y salen sin tocar nada. Los botones locales del
servidor no pasan por ese filtro.

**Puerto.** 1099 (el estándar de RMI). El registro lo crea el propio servidor con
`LocateRegistry.createRegistry`, así que no hay que lanzar `rmiregistry` a mano.

---