// global on load
global.listeners.uiconfig.push(
    async function(uiconfig){
        console.log('custom operation on load ' + JSON.stringify(uiconfig));
    }
)

// function is executed when socket message arrives
function functionalProcessing(jsonMsg){
    data = jsonMsg.data;
    type = jsonMsg.type;
    // do you custom functional stuff here
    switch (type) {
        case "person":
            jsonMsg.formatting = {
                vote: function (value){
                    if (value=="yes"){
                        return "positive";
                    }else{
                        return "negative";
                    }
                }
            }
            break;
        default:
          console.log("no specific functional processing for type " + type);
      }

}


function upload() {

    var f = document.getElementById('file');
    var fileName = document.getElementById('fileName').value;
    if (f.files.length)
    {
        var xhr = new XMLHttpRequest();

        xhr.upload.onprogress = function(event){
              // limit calls to this function
            if (!this.NextSecond) this.NextSecond = 0;
            if ((new Date()).getTime() < this.NextSecond) return;
            this.NextSecond = (new Date()).getTime() + 250;

            data = {
                id: fileName,
                status:  Math.round(event.loaded / event.total * 100) + "%"
            }
    
            dataFormatedForView = {
                elementIds: ["upload-state-table"],
                actions: ["notify","update-header", "upsert-data"],
                type:"upload",
                data: [data]
            }
            var viewEvent ={
                data: JSON.stringify(dataFormatedForView)
            } 
            
            processSocketMsg(viewEvent);
        };


        xhr.onreadystatechange = function() { // Call a function when the state changes.
            if (this.readyState === XMLHttpRequest.DONE && this.status === 200) {

                data = {
                    id: fileName,
                    status: "done"
                }

                dataFormatedForView = {
                    elementIds: ["upload-state-table"],
                    actions: ["notify","update-header", "upsert-data"],
                    type:"upload",
                    data: [data]
                }
                var event ={
                    data: JSON.stringify(dataFormatedForView)
                } 
                processSocketMsg(event);
            }
        }

        xhr.open('post', '/upload/'+fileName, true);
        xhr.setRequestHeader("Content-Type","application/octet-stream");
        xhr.send(f.files[0]);
    }
    
}