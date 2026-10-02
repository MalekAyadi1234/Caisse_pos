# Firebase — ce que j’ai fait

1. Console Firebase → nouveau projet (j’ai appelé le mien un truc du style caisse-pos). Analytics off, pas besoin.

2. Ajouter une app Android, package `com.caisse.pos` (pareil que dans le gradle). Télécharger `google-services.json` → le mettre dans `app/`.

3. Build → Realtime Database → créer.
   Firebase te demande des règles, c’est normal. Prends **mode test**, ou colle ça dans l’onglet Règles puis Publier :

```
{
  "rules": {
    ".read": true,
    ".write": true
  }
}
```

Oui c’est ouvert, c’est juste pour le TP.

4. Lance l’app, encaisse une vente avec du réseau → tu dois voir un truc sous `sales/` dans la console. En mode avion ça attend, dès que tu reconnectes ça remonte.
