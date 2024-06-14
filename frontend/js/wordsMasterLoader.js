import { SSEClient } from "./libs/sse-client.js";
import { CardsView } from "./views/cards-view.js";
 

const playerId = JSON.parse(localStorage.getItem("current_player")).id;
const sseClient = new SSEClient("localhost:8080");
sseClient.connect();
sseClient.subscribe(playerId, (data) => {onSSEData(data)});

function onLoad()
{
    const view = new CardsView();

    const select_nbr = document.getElementById("nombre_indice");
    for(let i = 1; i < 10; i++)
    {
        const option = document.createElement("option");
        option.text = i;
        select_nbr.add(option);
    }
}
window.addEventListener("load", onLoad);

document.getElementById("btn_valider").addEventListener("click",()=>{
    sendClue()
})

async function sendClue(){
    let clue=document.getElementById("indice_input").value
    let toFind=document.getElementById("nombre_indice").value
    const id_partie=JSON.parse(localStorage.getItem("current_player"))
    const _clue= await fetch("http://localhost:8080/clue/"+id_partie.idPartie,{method:"post",headers: {"Content-Type": "application/json"},body:JSON.stringify(clue,toFind)})
    if(_clue.status==200){
        console.log(clue)
    }
    else{
        alert(_clue.text())
    }

}
