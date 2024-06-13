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
async function roleSwap(){
    //change de role, y a pas de sse pour l'instant donc change pas le role des deux joueurs.
    const data=localStorage.getItem("game_data")
    const id_partie=JSON.parse(data).id;


    const role= await fetch("http://localhost:8080/role/swap/"+id_partie,{method:"post"})

    const label_intuition = document.getElementById("MAITRE_INTUITION");
    const label_maitre_mot = document.getElementById("MAITRE_MOT");
    
    label_intuition.innerHTML = "";
    label_maitre_mot.innerHTML = ""; 

    if(role.status==200)
    {
        const role_payload =await role.json()

        const p1 = role_payload.filter(e => {return e.role==="MAITRE_INTUITION" })[0];
        if(p1)
            label_intuition.innerHTML = p1.nom ?? "";

        const p2 = role_payload.filter(e => {return e.role==="MAITRE_MOT" })[0];
        if(p2)
            label_maitre_mot.innerHTML = p2.nom ?? "";
    }

    if(role.status==500)
    {
        alert(await role.text())
    }
}

async function start(id){
    const words = await fetch("http://localhost:8080/start/"+id)
    if(words.status==200){
        localStorage.setItem("word_list",await words.text())
        const body = await words.json()
        
        const player = body.filter(p => p.id() === idJoueur)[0]
        if(player.role === "MAITRE_INTUITON")
            {
                window.location.href="/frontend/intuitionMaster.html"
            }
            else
            {
                window.location.href="/frontend/wordsMaster.html"   
            }
    }
}