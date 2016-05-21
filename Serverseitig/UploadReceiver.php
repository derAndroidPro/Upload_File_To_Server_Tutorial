<?php
$uploadName = "fileupload1";
$uploadFolder = "u/";
$fullServerLink = "http://derandroidpro.esy.es/upload_file_to_server_tutorial/";
if(!file_exists($uploadFolder)){
	mkdir($uploadFolder, 0777);
}

$temp_name = $_FILES[$uploadName]['tmp_name'];
$original_name = $_FILES[$uploadName]['name'];
$filesize = $_FILES[$uploadName]['size'];

if($temp_name != "" && exif_imagetype($temp_name) != false && $filesize <= 10485760 ){
	$file_extension = ".".end((explode(".", $original_name)));
	$newFile = $uploadFolder.round(microtime(true)*1000).$file_extension;
	if(move_uploaded_file($temp_name, $newFile)){
		echo $fullServerLink.$newFile;
	} else {
		echo utf8_encode("Datei konnte auf dem Server nicht gespeichert werden. Bitte erneut versuchen!");
	}
} else {
	echo utf8_encode("Kein (echtes) Bild erhalten. (Maximale Dateigrφίe: 10MB");
}

?>