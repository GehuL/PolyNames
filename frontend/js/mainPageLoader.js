

function run(){
    document.getElementById("createGame").addEventListener("click",()=>{
        newGame();
    })
    document.getElementById("joinGame").addEventListener("click",()=>{
        _pseudo=document.getElementsByName("PSEUDO")//non plus
        loadGame(_pseudo);
    })
}


window.addEventListener("load",run)

const baseURI = "http://localhost:5500/"



async function newGame(){
    let game =  await fetch("http://localhost:8080/createGame",{method:"put"});
    const pseudo = "toto";

    if(game.status==200){

        //window.location.href="/frontend/roleChoice.html"
        localStorage.setItem("game_data",await game.json());


        //console.log( game.json())
        //loadGame(pseudo)  
        
       
    }
    return null;
}

async function loadGame(pseudo){
    const load = await fetch("http://localhost:8080/joinGame/"+"pseudo")
    if(load.status==200){
        const sseClient =  new sseClient("http://localhost:8080");
        await sseClient.connect();  
        window.location.href= "../roleChoice.html"
    }
    return null;
}
