import { SSEClient } from "./libs/sse-client.js";
import { ApiService } from "./services/api-service.js";
import { RoleView } from "./views/role-view.js";

const sseClient = new SSEClient("localhost:8080");
sseClient.connect();

window.addEventListener("load",run)

async function run()
{
    document.getElementById("role_swap").addEventListener("click", fetchSwap);

    document.getElementById("start").addEventListener("click",()=>{
        start(false);
    })

    document.getElementById("random").addEventListener("click",()=>{
        start(true);
    })
    document.getElementById("copy").addEventListener("click",function(){
        const code = localStorage.getItem("gameCode")
        console.log(code)
        navigator.clipboard.writeText(code)
    })

    const players = await ApiService.getPlayers()
    new RoleView().updateRole(await players.json());

    const playerId = JSON.parse(localStorage.getItem("current_player")).id;

    sseClient.subscribe(playerId, (data) => {onSSEData(data)});

    const gameCode = localStorage.getItem("gameCode");

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

function onSSEData(data)
{
    console.log(data);

    // La partie est lancé, il faut changer de page
    if(data?.etat == "CHOISIR_INDICE")
    {
        enterGame(data.role);
    }else
    {
        const roleView = new RoleView();
        roleView.updateRole(data);
    }
}

function enterGame(role)
{
    if(role == "MAITRE_INTUITION")
    {
        window.location.href="intuitionMaster.html"
    }
    else
    {
        window.location.href="wordsMaster.html"   
    }
}

/** Commence la partie et change de page
 * @param {*} randomly Indique si il faut démarrer la partie avec les roles aléatoires
 */
async function start(randomly)
{
    const response = await ApiService.startGame(randomly);

    if(response.status==200 && response?.etat == "CHOISIR_INDICE")
    {
        const payload = await response.json();
        enterGame(payload.role);       
    }else
    {
        alert(await response.text())
    }
}