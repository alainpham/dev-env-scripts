// global on load
global.listeners.uiconfig.push(
    async function(uiconfig){
        console.log('custom operation on load ' + JSON.stringify(uiconfig));
    }
)

// function is executed when socket message arrives
function functionalProcessing(jsonMsg){
    data = jsonMsg.data;
    console.log(data);
    type = jsonMsg.type;
    console.log(type);
    // do you custom functional stuff here
    switch (type) {
        case "balance":
            jsonMsg.formatting = {
                status: function (value){
                    if (value == "success"){
                        return "positive";
                    }else if (value == "failed"){
                        return "negative";
                    }else if (value == "clearing"){
                        return "warn";
                    }
                }
            }
            break;
        default:
          console.log("no specific functional processing for type " + type);
      }

}