window.addEventListener("load",run)

function run(){
    document.getElementById("role_swap").addEventListener("click",()=>{
        roleSwap();

    })
    const data=localStorage.getItem("game_data")
    const gameCode=JSON.parse(data).code
    document.getElementById("game_code").innerHTML="Partagez le code de la partie "+gameCode


}



async function roleSwap(){
    const data=localStorage.getItem("game_data")
    const id_partie=JSON.parse(data).id;

    const role= await fetch("http://localhost:8080/role/swap/"+id_partie,{method:"post"})

    if(role.status==200){
        let i =await role.json()
        document.getElementById("current_role").innerHTML="Vous etes actuellement " +i[0].role
        
    }
    if(role.status==500){
        alert("ca n a pas marche ")
    }

    
}
