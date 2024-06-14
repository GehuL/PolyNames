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
        card.addEventListener("click",()=>{
            guess(card.innerHTML)
        })
    }
}

function onSSEData(data)
{
    if(data?.clue)
    {

    }
}

async function guess(word){
    const id_partie=JSON.parse(localStorage.getItem("current_player"))
    const guess = await fetch("http://localhost:8080/guess/"+id_partie.idPartie,{method:"post",headers: {"Content-Type": "application/json"},body:JSON.stringify(word)})
    if(guess.status==200){
        console.log("indice envoyé")
    }
    else{
        alert(guess.text())
    }
}


window.addEventListener("load", onLoad);

