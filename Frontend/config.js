/**
 * Created by snadal on 17/05/16.
 */

var config = {};

config.PORT = 3000;
//
config.METADATA_FRONTEND_URL = "http://localhost:" + config.PORT + "/";
config.METADATA_DATA_LAYER_URL = "http://localhost:8082/metadataStorage/";
config.ONTO_MATCH_MERGE_URL = "http://localhost:8082/ontoMatchMerge/";
config.DEFAULT_NAMESPACE = "http://supersede/";
config.FILES_PATH = "/home/kashif/Documents/GIT/MDM/MetadataFrontend";



config.BDI_FRONTEND_URL = "http://localhost:" + config.PORT + "/";
config.BDI_DATA_LAYER_URL = "http://localhost:8080/bdi/";
config.BDI_FILES_PATH = "/home/kashif/Documents/GIT/BDI/Frontend/Uploads/";

module.exports = config;