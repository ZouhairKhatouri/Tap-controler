<?php
    $clientID = $_POST["clientID"];
    // Hashing
    $hashedID = sha1($clientID);
    // Getting the content of the JSON file 
    $dataForEachClient_json = file_get_contents("dataForEachClient.json");
    // Decoding
    $dataForEachClient_array = json_decode($dataForEachClient_json, true);
    // Echo
    if (array_key_exists($hashedID,$dataForEachClient_array))
    {
        echo "*".$dataForEachClient_array[$hashedID]."*";
    }
    else
    {
        echo "*"."F#F"."*";
    }
?>
