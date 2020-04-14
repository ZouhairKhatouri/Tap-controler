<?php
    $clientID = $_POST["clientID"];
    $flow = $_POST["flow"];
    $duration = $_POST["duration"];
    // Hashing
    $hashedID = sha1($clientID);
    // Getting the content of the JSON file 
    $dataForEachClient_json = file_get_contents("dataForEachClient.json");
    // Decoding
    $dataForEachClient_array = json_decode($dataForEachClient_json, true);
    if (array_key_exists($hashedID,$dataForEachClient_array))
    {
        $dataForEachClient_array[$hashedID]="$flow"."#"."$duration";
        $dataForEachClient_json = json_encode($dataForEachClient_array);
        file_put_contents("dataForEachClient.json",$dataForEachClient_json);
    }
?>