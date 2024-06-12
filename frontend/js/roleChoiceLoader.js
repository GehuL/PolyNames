window.addEventListener("load",run)

async function run()
{
    document.getElementById("role_swap").addEventListener("click",()=>{
        roleSwap();

    })

    document.getElementById("start").addEventListener("click",()=>{
        start(id_partie)
    })
    const data=localStorage.getItem("game_data")
    const gameCode=JSON.parse(data).code
    //document.getElementById("game_code").innerHTML="Partagez le code de la partie "+gameCode
    const id_partie=JSON.parse(data).id;

    document.getElementById("room_name").innerHTML = "ROOM #" + gameCode;
}

async function roleSwap(){
    //change de role, y a pas de sse pour l'instant donc change pas le role des deux joueurs.
    const data=localStorage.getItem("game_data")
    const id_partie=JSON.parse(data).id;

    const role= await fetch("http://localhost:8080/role/swap/"+id_partie,{method:"post"})

    if(role.status==200){
        let role_payload =await role.json()

        if(role_payload.length > 0){
            if(role_payload[0].role="Maitre des mots"){//syntaxe a verifier
                document.getElementById("nom_player2").innerHTML=role_payload[0].pseudo
                document.getElementById("nom_player1").innerHTML=role_payload[1].pseudo         
            }
            else{
                document.getElementById("nom_player2").innerHTML=role_payload[1].pseudo
                document.getElementById("nom_player1").innerHTML=role_payload[0].pseudo
            }
        }        
    }
    if(role.status==500){
        alert("ca n a pas marche ")
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