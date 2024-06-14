import { SSEClient } from "./libs/sse-client.js";
import { CardsView } from "./views/cards-view.js";
import { ApiService} from "./services/api-service.js"

const playerId = JSON.parse(localStorage.getItem("current_player")).id;
const partieId = JSON.parse(localStorage.getItem("current_player")).idPartie;

const apiService = new ApiService(partieId, playerId)
const sseClient = new SSEClient("localhost:8080");

sseClient.connect();
sseClient.subscribe(playerId, (data) => {onSSEData(data)});
 
async function onLoad()
{
    const view = new CardsView();   

    const cards = await apiService.getCards();
    
    for(let card of await cards.json())
    {
        view.displayCard(card);
    }

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

function onSSEData(data)
{
    if(data?.etatPartie == "FIN")
    {
        alert("La partie est finie !");
        window.location.href="/index.html";
    }
}

async function sendClue()
{
    if(document.getElementById("indice_input").value=="")
    {
        alert("Indice non valide")
        return
    }

    let clue = document.getElementById("indice_input").value
    let toFind = document.getElementById("nombre_indice").value

    const body = JSON.stringify({"clue":clue, "toFind": toFind});

    const _clue= await fetch("http://localhost:8080/clue/"+partieId,{method:"post",headers: {"Content-Type": "application/json"}, body:body})
    if(_clue.status==200)
    {
       
    }
    else
    {
        alert(await _clue.text())
    }

}
