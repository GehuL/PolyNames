window.addEventListener("load",run)

async function run()
{
    document.getElementById("role_swap").addEventListener("click",()=>{
        roleSwap();

    })

    document.getElementById("start").addEventListener("click",()=>{
        start();
    })

    const data=localStorage.getItem("game_data")
    const gameCode=JSON.parse(data).code

    document.getElementById("room_name").innerHTML = "ROOM #" + gameCode;
}

// change de role, c'est a dire intervertit les role si il y a deux joueurs, ou change simplement le role si un seul joueur est dans la partie
async function roleSwap()
{
    const id_partie= JSON.parse(localStorage.getItem("current_player")).idPartie;

    const role= await fetch("http://localhost:8080/role/swap/"+id_partie,{method:"post"})

    const label_intuition = document.getElementById("MAITRE_INTUITION");
    const label_maitre_mot = document.getElementById("MAITRE_MOT");
    
    label_intuition.innerHTML = "";
    label_maitre_mot.innerHTML = ""; 

    if(role.status==200)
    {
        const role_payload =await role.json()

        // Cherche le joueur avec le role maitre intuition
        const p1 = role_payload.filter(e => {return e.role==="MAITRE_INTUITION" })[0];
        if(p1)
            label_intuition.innerHTML = p1.nom ?? "";

        // Cherche le joueur avec le role maitre mots
        const p2 = role_payload.filter(e => {return e.role==="MAITRE_MOT" })[0];
        if(p2)
            label_maitre_mot.innerHTML = p2.nom ?? "";

        // Stock les infos du joueur actuelle
        const idJoueur = JSON.parse(localStorage.getItem("current_player")).id;
        localStorage.setItem("current_player", JSON.stringify(role_payload.filter(e => {return e.id==idJoueur})[0]));
    }

    if(role.status==500)
    {
        alert(await role.text())
    }
}

async function start()
{
    const partieId = JSON.parse(localStorage.getItem("current_player")).idPartie;
    const response = await fetch("http://localhost:8080/start/"+partieId, {"method": "put"})

    if(response.status==200)
    {
        const body = await response.json()
        
        const currentPlayer = JSON.parse(localStorage.getItem("current_player"));

        if(currentPlayer.role === "MAITRE_INTUITON")
        {
            window.location.href="/intuitionMaster.html"
        }
        else
        {
            window.location.href="/wordsMaster.html"   
        }
    }else
    {
        alert(await response.text())
    }
}