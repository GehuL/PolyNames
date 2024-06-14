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

    let div_cards = document.getElementsByClassName("cards")
    for(let card of div_cards){
        card.addEventListener("click",(e)=>{
            console.log(e);
            guess(e.target.getAttribute("id"));
        })
    }
}

function onSSEData(data)
{
    if(data?.clue)
    {
        document.getElementById("indice").innerHTML = data.clue;
        document.getElementById("nombre_deviner").innerHTML = data.toFind;
    }
}

async function guess(idCard)
{
    const guess = await apiService.guess(idCard);
    
    if(guess.status==200)
    {
        const payload = await guess.json();

        document.getElementById("score").innerHTML ="SCORE: " + payload.score;
        document.getElementById(payload.idCard).dataset.color = payload.color;

        if(payload.etatPartie == "FIN")
        {
            alert("La partie est finie !");
            window.location.href="/index.html";
        }
    }
    else
    {
        alert(await guess.text())
    }
}


window.addEventListener("load", onLoad);

