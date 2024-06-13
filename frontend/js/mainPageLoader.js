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
{
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
{
    const placeHolderPseudo=document.getElementById("pseudo_placeholder").value
    const name={nom:placeHolderPseudo}
    const load = await fetch("http://localhost:8080/joinGame/"+code,{method:"put",headers: {"Content-Type": "application/json"},body:JSON.stringify(name)})// load contient l'id du joueur qui a fait la requete 
    
    if(load.status==200)
    {
        /*const sseClient =  new sseClient("http://localhost:8080");
        await sseClient.connect();
        console.log("connecte au sse client")*/
        //console.log(await load.json())
        const payload = await load.json();
        
        localStorage.setItem("playerId", payload.id);
        localStorage.setItem("partieId", payload.idPartie);

        window.location.href= baseURI+"/roleChoice.html"
    }else
    {
        alert(await load.text());
    }
}
