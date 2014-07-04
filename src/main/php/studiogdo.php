<!DOCTYPE html>
<html>
   <head>
      <meta charset='utf-8'>
      <title>PHP StudioGDO Wrapper</title>
   </head>
   <body>
   <?php 
      class Wrapper
      {
         public static $APNAME;
         private $sessionID;

         private function callStudio($url)
         {
            // Ajouter le contexte
            $opts = array(
               'http'=> array(
                  'method' => 'GET',
                  'ignore_errors' => True,
                  'header' => 'Cookie: JSESSIONID=F07901BE1C1334B94FE6E2E9AB1F5F0A\r\n'));

            $ctx = stream_context_create($opts);
            // Apelle le serveur
            $req = file_get_contents($url, False, $ctx);
            if ($req == False) {
               return "Erreur";
            }
            // Récupère la session en cours
            $xsess = simplexml_load_string($req);
            // Retourne le tout
            return $req;
         }

         public function getStencils ($path)
         {
            $args = "&a=";
            for ($i = 1; $i < func_num_args() ; $i ++) {
               // XXX Mettre en place un contrôle des entrées
               $args .= ":" . urlencode(func_get_arg($i));
            }
            $url = "http://" . Wrapper::$APNAME . "/local/rpc/stencils.gdo?p=" . $path . $args;

            $result = simplexml_load_string($this->callStudio($url));
            return $result;
            //$lprop = $result->xpath('/result/status/stencils/stencil');
            //return $lprop;
         }
      }
   ?>

   <h1>Wrapper pour l'utilisation de la base StudioGDO</h1>
   <p>Vous ne devriez pas utiliser ce fichier à des fins autres que du
   test!</p>

   <article id="Session Test">
      <h2> Lancement du test de base </h2>
      <?php 
      Wrapper::$APNAME = "192.168.1.24:8080/morassuti";
      $con = new Wrapper();
      $res = $con->getStencils('/Services(affiches)/Commande(50)','PaysLiv');
      foreach ($res as $r) {
         echo $r;
      }
	  ?>
   </article>

   </body>
</html>
