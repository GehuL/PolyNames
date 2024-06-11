window.addEventListener("load",run)

function run(){
    document.getElementById("wordsMaster").addEventListener("click",()=>{
        Choice("wordsMaster");
        const start = fetch("hhtp://localhost:8080/start/:")// il manque l'idPartie
        window.location.replace("../wordsMaster.html")

    })

    document.getElementById("intuitionMaster").addEventListener("click",()=>{
        Choice("intuitionMaster")
        const start = fetch("hhtp://localhost:8080/start/:")// il manque l'idPartie
        window.location.replace("../intuitionMaster.html")

    })

    document.getElementById("random").addEventListener("click",()=>{
        randomChoice();

    })
}


async function randomChoice(){
    const role= await fetch("http://localhost:8080/role/random")
    if(role.status ==200){
        //rediriger et affecter le role 
    }
    return null
}

async function Choice(role){
    const role= await fetch("http://localhost:8080/"+role)//il manque l idplayer


}
