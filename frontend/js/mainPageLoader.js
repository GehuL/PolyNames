function run(){
    document.getElementById("createGame").addEventListener("click",()=>{
        newGame();
    })
    
    document.getElementById("joinGame").addEventListener("click",()=>{
        
        const placeHolderCode=document.getElementById("code_placeholder").value
        const placeHolderPseudo=document.getElementById("pseudo_placeholder").value
        console.log(placeHolderCode)
        console.log(placeHolderPseudo)
        loadGame(placeHolderCode);
    })

}

window.addEventListener("load",run)

const baseURI = "http://localhost:5500"

async function newGame()
{   if(document.getElementById("pseudo_placeholder").value==""){
    alert("Pseudo non valide")
    return
}
    let game =  await fetch("http://localhost:8080/createGame",{method:"put"});

    if(game.status==200)
    {
        localStorage.setItem("game_data",await game.text());
        let data= localStorage.getItem("game_data")
        let code = JSON.parse(data).code;
        console.log(code)
        loadGame(code)
    }   
}

async function loadGame(code)
{   if(document.getElementById("pseudo_placeholder").value==""){
        alert("Pseudo non valide")
        return
    }
    const placeHolderPseudo=document.getElementById("pseudo_placeholder").value
    const name={nom:placeHolderPseudo}
    const load = await fetch("http://localhost:8080/joinGame/"+code,{method:"put",headers: {"Content-Type": "application/json"},body:JSON.stringify(name)})// load contient l'id du joueur qui a fait la requete 
    
    if(load.status==200)
    {
        const payload = await load.json();
        // Sauvegarde l'id du joueur pour garder une trace et actualiser les infos envoyées par le serveur
        localStorage.setItem("current_player", JSON.stringify(payload));
        localStorage.setItem("gameCode", code);
        window.location.href= "roleChoice.html"
    }else
    {
        alert(await load.text());
    }
}
