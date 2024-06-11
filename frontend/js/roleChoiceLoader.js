window.addEventListener("load",run)

function run(){
    document.getElementById("role_swap").addEventListener("click",()=>{
        roleSwap();

    })
}


/*async function randomChoice(){
    const role= await fetch("http://localhost:8080/role/random")
    if(role.status ==200){
        //rediriger et affecter le role 
    }
    return null
}*/

async function roleSwap(){
    const role= await fetch("http://localhost:8080/role/swap/"+id_partie)//il manque idpartie


}
