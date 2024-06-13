import { SSEClient } from "./libs/sse-client.js";
import { RoleView } from "./views/role-view.js";
import { ApiService } from "./services/api-service.js";

const baseURL = "http://localhost:8080";
const sseClient = new SSEClient("localhost:8080");
sseClient.connect();

window.addEventListener("load",run)

async function run()
{
    connectSSE();

    document.getElementById("role_swap").addEventListener("click", fetchSwap);

    document.getElementById("start").addEventListener("click",()=>{
        start(false);
    })

    document.getElementById("random").addEventListener("click",()=>{
        start(true);
    })

    const playerId = JSON.parse(localStorage.getItem("current_player")).id;


    sseClient.subscribe(playerId, (data) => {onSSEData(data)});

    const data=localStorage.getItem("game_data")
    const gameCode=JSON.parse(data).code

    document.getElementById("room_name").innerHTML = "ROOM #" + gameCode;
}


// change de role, c'est a dire intervertit les role si il y a deux joueurs, ou change simplement le role si un seul joueur est dans la partie
async function fetchSwap()
{
    const response = await ApiService.swapRole();

    if(response.status==200)
    {
        const players = await response.json();
        new RoleView().updateRole(players);
    }else
    {
        alert(await response.text())
    }
}

function connectSSE() {
  
}

function onSSEData(data)
{
    // La partie est lancé, il faut changer de page
    if(data?.etat === "CHOISIR_INDICE")
    {
        enterGame();
    }else
    {
        const roleView = new RoleView();
        roleView.updateRole(data);
    }

}

function enterGame()
{
    const currentPlayer = JSON.parse(localStorage.getItem("current_player"));

    if(currentPlayer.role === "MAITRE_INTUITON")
    {
        window.location.href="/intuitionMaster.html"
    }
    else
    {
        window.location.href="/wordsMaster.html"   
    }
}

/** Commence la partie et change de page
 * @param {*} randomly Indique si il faut démarrer la partie avec les roles aléatoires
 */
async function start(randomly)
{
    const response = await ApiService.startGame(randomly);

    if(response.status==200)
    {
        enterGame();       
    }else
    {
        alert(await response.text())
    }
}