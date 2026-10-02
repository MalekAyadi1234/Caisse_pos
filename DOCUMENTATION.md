# Doc projet — mini caisse

Salut, voilà comment faire tourner le truc et ce que j’ai mis en place.

## Lancer / tester

Tu ouvres le dossier dans Android Studio, tu laisses Gradle faire son sync (ça peut prendre un moment la première fois).

Avant de Run, faut avoir Firebase : j’ai noté les étapes dans `docs/FIREBASE.md`. En gros tu crées un projet, tu ajoutes l’app Android (`com.caisse.pos`), tu balances le `google-services.json` dans `app/`, tu crées une Realtime Database et tu mets des règles ouvertes (mode test). Après ça Run sur un émulateur ou ton téléphone.

Pour checker que ça marche :
- tu tapes 2-3 produits, Encaisser → toast avec le n° de ticket, panier vide
- menu Historique en haut → tu vois n°, montant, état impression
- mode avion, tu encaisse encore → ça passe quand même, historique OK
- tu remets le wifi → ça finit en Sync OK et ça apparaît dans la console Firebase
- si tu kill l’app alors qu’un ticket est encore "en attente", au relancement il réessaie d’imprimer (regarde Logcat, tag `TicketPrinter`). Ceux déjà imprimés on les touche pas.

Y’a aussi un petit test unitaire si tu veux : `./gradlew test`

## Archi (ce que j’ai fait)

Honnêtement j’ai essayé de rester simple.

Deux écrans : la caisse (produits + panier + Encaisser) et l’historique.

Quand tu encaisse, tout passe par `SaleRepository`. L’idée c’était : d’abord écrire en local (Room), vider le panier direct pour que l’UI réponde vite, et après seulement (en async) impression + envoi Firebase. Comme ça même sans réseau ça reste fluide.

Room = ce qui compte vraiment chez moi. Firebase = sync cloud. Pour l’imprimante j’ai juste simulé (log Logcat), j’avais pas d’imprimante sous la main.

## Données

Local :
- table `sales` → id, n° ticket, device, séquence, total, lignes (JSON), date, print status, synced ou pas…
- table `ticket_counter` → juste le prochain numéro

Firebase : `sales/{saleId}` avec à peu près la même chose.

J’ai mis le `saleId` (UUID) en clé Firebase, pas le n° de ticket. Pourquoi : si on renvoie la même vente deux fois (retry), ça écrase, ça ne crée pas un doublon.

## Unicité des n° de ticket

C’était le point un peu chiant du sujet.

Au début je me suis dit "compteur global sur Firebase", mais dès que t’es offline ça casse : deux tablettes peuvent prendre le même numéro.

Donc j’ai fait un numéro **par appareil** : genre `A3F2-000042`.
- `A3F2` = petit préfixe généré à l’install (gardé dans les prefs)
- `000042` = compteur local Room, incrémenté tranquillement même sans wifi

Comme ça chaque caisse a sa file, offline ou pas.

**Limite** (je le dis clairement) : y’a pas une seule séquence mondiale. Deux tablettes hors ligne en même temps → `A3F2-000001` et `B7C1-000001` existent en parallèle. Pour une mini caisse je trouve ça acceptable. Et surtout on renumérote jamais après coup (les tickets papier resteraient faux sinon).

Si tu désinstalles l’app, nouveau préfixe, compteur qui repart à 1.

## Offline / reconnexion / pas de perte

Dès l’encaissement la vente part en Room avec `synced = false`. Même si le wifi tombe ou que tu tues l’app juste après, elle est là.

Quand Firebase redevient joignable (j’écoute `.info/connected`), je pousse ce qui n’est pas encore syncé. Clé = saleId → pas de doublon.

Au démarrage : je ne réimprime que les tickets en attente ou en échec.

L’historique lit Room, donc tu peux le consulter hors ligne sans souci.
